package temp.okulyk.wos.chooseforme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import temp.okulyk.wos.chooseforme.model.card.Card;
import temp.okulyk.wos.chooseforme.model.wheel.RegularWheel;
import temp.okulyk.wos.chooseforme.model.wheel.SpecialWheel;

import static java.lang.Math.max;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static temp.okulyk.wos.chooseforme.model.card.CardType.REGULAR;
import static temp.okulyk.wos.chooseforme.model.card.CardType.SPECIAL;

public class ChooseForMe {
    public List<Card> findCards(List<Card> cards, RegularWheel regularWheel, SpecialWheel specialWheel) {
        List<Card> sortedCards = sortCards(cards);

        int maxPrice = regularWheel.getPrices().stream().max(Integer::compareTo).get();

        Map<Integer, Map<Integer, Map<Integer, Integer>>> possiblePrices = buildPricesTable(sortedCards, maxPrice, specialWheel.getSpecialCardsCount());
        int cardsCountToTake = possiblePrices.keySet().stream().max(Integer::compareTo).get();

        int targetPrice = possiblePrices.get(cardsCountToTake).get(maxPrice).get(specialWheel.getSpecialCardsCount());

        return findCardsToTakeFromTable(specialWheel, sortedCards, maxPrice, possiblePrices, cardsCountToTake, targetPrice);
    }

    private List<Card> findCardsToTakeFromTable(SpecialWheel specialWheel, List<Card> sortedCards, int maxPrice, Map<Integer, Map<Integer, Map<Integer, Integer>>> possiblePrices, int cardsCountToTake, int targetPrice) {
        int leftPrice = maxPrice;
        int leftSpecialCardsCount = specialWheel.getSpecialCardsCount();
        List<Card> result = new ArrayList<>();
        while (leftPrice > 0) {
            Card takenCard = sortedCards.get(cardsCountToTake);
            if (cardsCountToTake == 0) {
                result.add(takenCard);
                return result;
            }

            if (takeCurrentCard(possiblePrices, cardsCountToTake, targetPrice, leftPrice, leftSpecialCardsCount)) {
                leftPrice = addTakenCardToResult(leftPrice, result, takenCard);
            }
            if (takenCard.getCardType() == SPECIAL) {
                leftSpecialCardsCount--;
            }
            cardsCountToTake--;
        }
        return result;
    }

    private int addTakenCardToResult(int leftPrice, List<Card> result, Card takenCard) {
        leftPrice -= takenCard.getPrice();
        result.add(takenCard);
        return leftPrice;
    }

    private boolean takeCurrentCard(Map<Integer, Map<Integer, Map<Integer, Integer>>> possiblePrices, int cardsCountToTake, int targetPrice, int leftPrice,
                                    int leftSpecialCardsCount) {
        return safeGet(possiblePrices, cardsCountToTake - 1, targetPrice).get(leftSpecialCardsCount) != leftPrice;
    }

    private List<Card> sortCards(List<Card> cards) {
        return cards.stream()
            .sorted(comparing(Card::getDuplicateNumber, reverseOrder())
                .thenComparing(Card::getPrice))
            .collect(toList());
    }

    private Map<Integer, Integer> safeGet(Map<Integer, Map<Integer, Map<Integer, Integer>>> possiblePrices, int cardNumber, int price) {
        return Optional.ofNullable(possiblePrices.get(cardNumber))
            .map(priceForCardNumber -> priceForCardNumber.get(price))
            .orElse(new HashMap<>());
    }

    private Map<Integer, Map<Integer, Map<Integer, Integer>>> safePut(Map<Integer, Map<Integer, Map<Integer, Integer>>> possiblePrices, int cardNumber,
                                                                      int price,
                                                                      Map<Integer, Integer> pricesToPut) {
        Map<Integer, Map<Integer, Integer>> priceForCardNumber = possiblePrices.getOrDefault(cardNumber, new HashMap<>());
        priceForCardNumber.put(price, pricesToPut);
        possiblePrices.put(cardNumber, priceForCardNumber);
        return possiblePrices;
    }

    public Map<Integer, Map<Integer, Map<Integer, Integer>>> buildPricesTable(List<Card> sortedCards, int targetMaxPrice, int targetSpecialCardsCount) {
        Map<Integer, Map<Integer, Map<Integer, Integer>>> possiblePrices = new HashMap<>();

        for (int cardNumber = 0; cardNumber < sortedCards.size(); cardNumber++) {
            for (int currentPrice = 1; currentPrice <= targetMaxPrice; currentPrice++) {
                Card card = sortedCards.get(cardNumber);
                Map<Integer, Integer> doNotTakeMap = safeGet(possiblePrices, cardNumber - 1, currentPrice);
                int priceWithoutCurrentCard = currentPrice - card.getPrice();
                if (optionWithoutThisCardExist(priceWithoutCurrentCard)) {
                    Map<Integer, Integer> availablePricesWithoutCurrent = safeGet(possiblePrices, cardNumber - 1, priceWithoutCurrentCard);
                    Map<Integer, Integer> currentPrices = new HashMap<>();

                    if (card.getCardType() == REGULAR) {
                        calculateForRegular(cardNumber, currentPrice, card, doNotTakeMap, availablePricesWithoutCurrent, currentPrices);
                    } else {
                        calculateForSpecial(cardNumber, currentPrice, card, doNotTakeMap, availablePricesWithoutCurrent, currentPrices);
                    }
                    possiblePrices = safePut(possiblePrices, cardNumber, currentPrice, currentPrices);

                    if (currentPrices.getOrDefault(targetSpecialCardsCount, 0) == targetMaxPrice) {
                        return possiblePrices;
                    }
                } else {
                    possiblePrices = safePut(possiblePrices, cardNumber, currentPrice, doNotTakeMap);
                }
            }
        }

        System.out.println("Prices: " + possiblePrices);
        return possiblePrices;
    }

    private void calculateForSpecial(int cardNumber, int currentPrice, Card card, Map<Integer, Integer> doNotTakeMap,
                                     Map<Integer, Integer> availablePricesWithoutCurrent, Map<Integer, Integer> currentPrices) {
        int doNotTakePrice = doNotTakeMap.getOrDefault(0, 0);
        currentPrices.put(0, doNotTakePrice);
        for (int specialCardsCount = 0; specialCardsCount < cardNumber; specialCardsCount++) {
            doNotTakePrice = doNotTakeMap.getOrDefault(specialCardsCount + 1, 0);
            int takePrice = availablePricesWithoutCurrent.getOrDefault(specialCardsCount, 0) + card.getPrice();
            if (ableToTakeCard(currentPrice, takePrice)) {
                currentPrices.put(specialCardsCount + 1, max(doNotTakePrice, takePrice));
            } else {
                doNotTakePrice = doNotTakeMap.getOrDefault(specialCardsCount, 0);
                int existingPrice = currentPrices.getOrDefault(specialCardsCount, 0);
                currentPrices.put(specialCardsCount, max(doNotTakePrice, existingPrice));
            }
        }
    }

    private void calculateForRegular(int cardNumber, int currentPrice, Card card, Map<Integer, Integer> doNotTakeMap,
                                     Map<Integer, Integer> availablePricesWithoutCurrent, Map<Integer, Integer> currentPrices) {
        for (int specialCardsCount = 0; specialCardsCount <= cardNumber; specialCardsCount++) {
            int doNotTakePrice = doNotTakeMap.getOrDefault(specialCardsCount, 0);
            int takePrice = availablePricesWithoutCurrent.getOrDefault(specialCardsCount, 0) + card.getPrice();
            if (ableToTakeCard(currentPrice, takePrice)) {
                currentPrices.put(specialCardsCount, max(doNotTakePrice, takePrice));
            } else {
                currentPrices.put(specialCardsCount, doNotTakePrice);
            }
        }
    }

    private boolean ableToTakeCard(int currentPrice, int takePrice) {
        return takePrice <= currentPrice;
    }

    private boolean optionWithoutThisCardExist(int priceWithoutCurrentCard) {
        return priceWithoutCurrentCard >= 0;
    }
}
