package temp.okulyk.wos.chooseforme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        int[][] possiblePrices = new int[maxPrice][cards.size()];

        int bestPossiblePrice = 0;
        int cardsCountToTake = 0;
        for (int cardNumber = 0; cardNumber < cards.size(); cardNumber++) {
            for (int currentPrice = 1; currentPrice <= maxPrice; currentPrice++) {
                Card card = sortedCards.get(cardNumber);
                if (cardNumber == 0) {
                    if (card.getPrice() > currentPrice) {
                        possiblePrices[currentPrice - 1][cardNumber] = 0;
                    } else {
                        possiblePrices[currentPrice - 1][cardNumber] = card.getPrice();
                    }
                } else {
                    int doNotTake = possiblePrices[currentPrice - 1][cardNumber - 1];
                    int priceWithoutCurrentCard = currentPrice - card.getPrice();
                    if (priceWithoutCurrentCard > 0) {
                        int take = possiblePrices[priceWithoutCurrentCard - 1][cardNumber - 1] + card.getPrice();
                        bestPossiblePrice = Math.max(doNotTake, take);
                    } else {
                        bestPossiblePrice = doNotTake;
                    }
                    possiblePrices[currentPrice - 1][cardNumber] = bestPossiblePrice;
                }
            }
            if (bestPossiblePrice == maxPrice) {
                cardsCountToTake = cardNumber;
                break;
            }
        }

        System.out.println("Prices:");
        for (int card = 0; card < cards.size(); card++) {
            for (int price = 0; price < maxPrice; price++) {
                System.out.print(possiblePrices[price][card] + " ");
            }
            System.out.println();
        }

        if (bestPossiblePrice == maxPrice) {
            int leftPrice = bestPossiblePrice;
            List<Card> result = new ArrayList<>();
            while (leftPrice > 0) {
                if (possiblePrices[bestPossiblePrice - 1][cardsCountToTake] != maxPrice) {
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
}
