package temp.okulyk.wos.chooseforme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import temp.okulyk.wos.chooseforme.model.card.Card;
import temp.okulyk.wos.chooseforme.model.wheel.RegularWheel;
import temp.okulyk.wos.chooseforme.model.wheel.SpecialWheel;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static temp.okulyk.wos.chooseforme.model.card.CardType.REGULAR;

public class ChooseForMe {
    public List<Card> findCards(List<Card> cards, RegularWheel regularWheel, SpecialWheel specialWheel) {
        List<Card> sortedCards = sortCards(cards);

        int maxPrice = regularWheel.getPrices().stream().max(Integer::compareTo)
            .get();

        Result result1 = new Result(cards, specialWheel, sortedCards, maxPrice).invoke();

        Map<Integer, Map<Integer, Map<Integer, Integer>>> possiblePrices = result1.getPossiblePrices();
        int bestPossiblePrice = result1.getBestPossiblePrice();
        int cardsCountToTake = result1.getCardsCountToTake();

        if (cardsCountToTake > 0) {
            int leftPrice = maxPrice;
            int leftSpecialCardsCount = specialWheel.getSpecialCardsCount();
            List<Card> result = new ArrayList<>();
            while (leftPrice > 0) {
                Card takenCard = sortedCards.get(cardsCountToTake);
                if (cardsCountToTake == 0) {
                    result.add(takenCard);
                    return result;
                }

                if (takenCard.getCardType() == REGULAR) {
                    if (safeGet(possiblePrices, bestPossiblePrice, cardsCountToTake - 1).get(leftSpecialCardsCount) != leftPrice) {
                        leftPrice -= takenCard.getPrice();
                        result.add(takenCard);
                    }
                } else {
                    if (safeGet(possiblePrices, bestPossiblePrice, cardsCountToTake - 1).get(leftSpecialCardsCount) != leftPrice) {
                        leftPrice -= takenCard.getPrice();
                        leftSpecialCardsCount--;
                        result.add(takenCard);
                    }
                }
                cardsCountToTake--;
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    private List<Card> sortCards(List<Card> cards) {
        return cards.stream()
            .sorted(comparing(Card::getDuplicateNumber, reverseOrder())
                .thenComparing(Card::getPrice))
            .collect(toList());
    }

    private Map<Integer, Integer> safeGet(Map<Integer, Map<Integer, Map<Integer, Integer>>> possiblePrices, int price, int cardNumber) {
        return Optional.ofNullable(possiblePrices.get(price))
            .map(priceForCards -> priceForCards.get(cardNumber))
            .orElse(new HashMap<>());
    }

    private Map<Integer, Map<Integer, Map<Integer, Integer>>> safePut(Map<Integer, Map<Integer, Map<Integer, Integer>>> possiblePrices, int price, int cardNumber, Map<Integer, Integer> pricesToPut) {
        Map<Integer, Map<Integer, Integer>> priceForCards = possiblePrices.getOrDefault(price, new HashMap<>());
        priceForCards.put(cardNumber, pricesToPut);
        possiblePrices.put(price, priceForCards);
        return possiblePrices;
    }

    private class Result {
        private List<Card> cards;
        private SpecialWheel specialWheel;
        private List<Card> sortedCards;
        private int maxPrice;
        private Map<Integer, Map<Integer, Map<Integer, Integer>>> possiblePrices;
        private int bestPossiblePrice;
        private int cardsCountToTake;

        public Result(List<Card> cards, SpecialWheel specialWheel, List<Card> sortedCards, int maxPrice) {
            this.cards = cards;
            this.specialWheel = specialWheel;
            this.sortedCards = sortedCards;
            this.maxPrice = maxPrice;
        }

        public Map<Integer, Map<Integer, Map<Integer, Integer>>> getPossiblePrices() {
            return possiblePrices;
        }

        public int getBestPossiblePrice() {
            return bestPossiblePrice;
        }

        public int getCardsCountToTake() {
            return cardsCountToTake;
        }

        public Result invoke() {
            possiblePrices = new HashMap<>();

            bestPossiblePrice = 0;
            cardsCountToTake = 0;
            for (int cardNumber = 0; cardNumber < cards.size(); cardNumber++) {
                for (int currentPrice = 1; currentPrice <= maxPrice; currentPrice++) {
                    Card card = sortedCards.get(cardNumber);
                    if (cardNumber == 0) {
                        if (card.getPrice() <= currentPrice) {
                            HashMap<Integer, Map<Integer, Integer>> priceForCards = new HashMap<>();
                            HashMap<Integer, Integer> specialCountToPrice = new HashMap<>();
                            if (card.getCardType() == REGULAR) {
                                specialCountToPrice.put(0, card.getPrice());
                            } else {
                                specialCountToPrice.put(1, card.getPrice());
                            }
                            priceForCards.put(cardNumber, specialCountToPrice);
                            possiblePrices.put(currentPrice, priceForCards);
                        }
                    } else {
                        Map<Integer, Integer> doNotTakeMap = safeGet(possiblePrices, currentPrice, cardNumber - 1);
                        int priceWithoutCurrentCard = currentPrice - card.getPrice();
                        if (priceWithoutCurrentCard >= 0) {
                            Map<Integer, Integer> availablePricesWithoutCurrent = safeGet(possiblePrices, priceWithoutCurrentCard, cardNumber - 1);
                            Map<Integer, Integer> currentPrices = new HashMap<>();

                            if (card.getCardType() == REGULAR) {
                                for (int specialCardsCount = 0; specialCardsCount <= cardNumber; specialCardsCount++) {
                                    int doNotTakePrice = doNotTakeMap.getOrDefault(specialCardsCount, 0);
                                    int takePrice = availablePricesWithoutCurrent.getOrDefault(specialCardsCount, 0) + card.getPrice();
                                    if (takePrice <= currentPrice) {
                                        bestPossiblePrice = Math.max(doNotTakePrice, takePrice);
                                        currentPrices.put(specialCardsCount, bestPossiblePrice);
                                    } else {
                                        currentPrices.put(specialCardsCount, doNotTakePrice);
                                    }
                                }
                            } else {
                                int doNotTakePrice = doNotTakeMap.getOrDefault(0, 0);
                                currentPrices.put(0, doNotTakePrice);
                                for (int specialCardsCount = 0; specialCardsCount < cardNumber; specialCardsCount++) {
                                    doNotTakePrice = doNotTakeMap.getOrDefault(specialCardsCount + 1, 0);
                                    int takePrice = availablePricesWithoutCurrent.getOrDefault(specialCardsCount, 0) + card.getPrice();
                                    if (takePrice <= currentPrice) {
                                        bestPossiblePrice = Math.max(doNotTakePrice, takePrice);
                                        currentPrices.put(specialCardsCount + 1, bestPossiblePrice);
                                    } else {
                                        doNotTakePrice = doNotTakeMap.getOrDefault(specialCardsCount, 0);
                                        if (doNotTakePrice > currentPrices.getOrDefault(specialCardsCount, doNotTakePrice)) {
                                            currentPrices.put(specialCardsCount, doNotTakePrice);
                                        }
                                    }
                                }
                            }
                            safePut(possiblePrices, currentPrice, cardNumber, currentPrices);

                            if (currentPrices.getOrDefault(specialWheel.getSpecialCardsCount(), 0) == maxPrice) {
                                cardsCountToTake = cardNumber;
                                return this;
                            }
                        } else {
                            safePut(possiblePrices, currentPrice, cardNumber, doNotTakeMap);
                        }
                    }
                }
            }

            System.out.println("Prices: " + possiblePrices);
            return this;
        }
    }
}
