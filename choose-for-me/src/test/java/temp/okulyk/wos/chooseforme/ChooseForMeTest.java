package temp.okulyk.wos.chooseforme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import temp.okulyk.wos.chooseforme.model.card.Card;
import temp.okulyk.wos.chooseforme.model.card.CardType;
import temp.okulyk.wos.chooseforme.model.wheel.RegularWheel;
import temp.okulyk.wos.chooseforme.model.wheel.SpecialWheel;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class ChooseForMeTest {

    private final ChooseForMe chooseForMe = new ChooseForMe();

    @Test
    public void regularWheel() throws IOException {
        List<String> lines = readLines("test1.in");
        List<Card> cards = readCards(lines, 2);

        List<Card> result = chooseForMe.findCards(cards, readRegularWheel(lines.get(0)), readSpecialWheel(lines.get(1)));
        Map<String, Long> cardsCount = countCards(result);

        assertEquals(readCardsResult(readLines("test1.out")), cardsCount);
    }

    @Test
    public void regularAndSpecialWheel() throws IOException {
        List<String> lines = readLines("test2.in");
        List<Card> cards = readCards(lines, 2);

        List<Card> result = chooseForMe.findCards(cards, readRegularWheel(lines.get(0)), readSpecialWheel(lines.get(1)));
        Map<String, Long> cardsCount = countCards(result);

        assertEquals(readCardsResult(readLines("test2.out")), cardsCount);
    }

    private List<String> readLines(String fileName) throws IOException {
        return Files.readAllLines(Paths.get(getClass().getClassLoader().getResource(fileName).getPath()));
    }

    private List<Card> readCards(List<String> lines, int indexToStart) {
        List<Card> cards = new ArrayList<>();
        for (int i = indexToStart; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] values = line.split(" ");
            String cardName = values[0];
            int rarity = parseInt(values[1]);
            CardType cardType = CardType.valueOf(values[2]);
            int count = parseInt(values[3]);
            for (int j = 0; j < count; j++) {
                cards.add(new Card(cardName, rarity, cardType, j));
            }
        }
        return cards;
    }

    private RegularWheel readRegularWheel(String line) {
        List<Integer> regularWheelPrices = stream(line.split(" "))
            .mapToInt(Integer::parseInt)
            .boxed()
            .collect(toList());
        return new RegularWheel(regularWheelPrices);
    }

    private SpecialWheel readSpecialWheel(String line) {
        return new SpecialWheel(parseInt(line));
    }

    private Map<String, Long> readCardsResult(List<String> lines) {
        Map<String, Long> result = new HashMap<>();
        for (String line : lines) {
            String[] split = line.split(" ");
            result.put(split[0], parseLong(split[1]));
        }
        return result;
    }

    private Map<String, Long> countCards(List<Card> cards) {
        return cards.stream()
            .collect(groupingBy(Card::getCardName, counting()));
    }
}