package temp.okulyk.wos.chooseforme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import temp.okulyk.wos.chooseforme.model.card.Card;
import temp.okulyk.wos.chooseforme.model.wheel.RegularWheel;
import temp.okulyk.wos.chooseforme.model.wheel.SpecialWheel;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

public class ChooseForMe {
    public List<Card> findCards(List<Card> cards, RegularWheel regularWheel, SpecialWheel specialWheel) {
        List<Card> sortedCards = sortCards(cards);

        int maxPrice = regularWheel.getPrices().stream().max(Integer::compareTo)
            .get();

        Map<Integer, Map<Integer, Integer>> possiblePrices = new HashMap<>();

        int bestPossiblePrice = 0;
        int cardsCountToTake = 0;
        for (int cardNumber = 0; cardNumber < cards.size(); cardNumber++) {
            for (int currentPrice = 1; currentPrice <= maxPrice; currentPrice++) {
                Card card = sortedCards.get(cardNumber);
                if (cardNumber == 0) {
                    if (card.getPrice() <= currentPrice) {
                        HashMap<Integer, Integer> priceForCards = new HashMap<>();
                        priceForCards.put(cardNumber, card.getPrice());
                        possiblePrices.put(currentPrice, priceForCards);
                    }
                } else {
                    int doNotTake = safeGet(possiblePrices, currentPrice, cardNumber - 1);
                    int priceWithoutCurrentCard = currentPrice - card.getPrice();
                    if (priceWithoutCurrentCard > 0) {
                        int take = safeGet(possiblePrices, priceWithoutCurrentCard, cardNumber - 1) + card.getPrice();
                        bestPossiblePrice = Math.max(doNotTake, take);
                    } else {
                        bestPossiblePrice = doNotTake;
                    }
                    possiblePrices = safePut(possiblePrices, currentPrice, cardNumber, bestPossiblePrice);
                }
            }
            if (bestPossiblePrice == maxPrice) {
                cardsCountToTake = cardNumber;
                break;
            }
        }

        System.out.println("Prices:");
        for (int card = 0; card < cards.size(); card++) {
            for (int price = 1; price < maxPrice; price++) {
                System.out.print(safeGet(possiblePrices, price, card) + " ");
            }
            System.out.println();
        }

        if (bestPossiblePrice == maxPrice) {
            int leftPrice = bestPossiblePrice;
            List<Card> result = new ArrayList<>();
            while (leftPrice > 0) {
                if (safeGet(possiblePrices, bestPossiblePrice, cardsCountToTake) != maxPrice) {
                    Card takenCard = cards.get(cardsCountToTake);
                    leftPrice -= takenCard.getPrice();
                    result.add(takenCard);
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

    private int safeGet(Map<Integer, Map<Integer, Integer>> possiblePrices, int price, int cardNumber) {
        Map<Integer, Integer> priceForCards = possiblePrices.get(price);
        if (priceForCards == null) {
            return 0;
        }
        return priceForCards.getOrDefault(cardNumber, 0);
    }

    private Map<Integer, Map<Integer, Integer>> safePut(Map<Integer, Map<Integer, Integer>> possiblePrices, int price, int cardNumber, int priceToPut) {
        Map<Integer, Integer> priceForCards = possiblePrices.getOrDefault(price, new HashMap<>());
        priceForCards.put(cardNumber, priceToPut);
        possiblePrices.put(price, priceForCards);
        return possiblePrices;
    }
}
