package temp.okulyk.wos.chooseforme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import temp.okulyk.wos.chooseforme.model.card.Card;
import temp.okulyk.wos.chooseforme.model.card.CardType;
import temp.okulyk.wos.chooseforme.model.wheel.RegularWheel;
import temp.okulyk.wos.chooseforme.model.wheel.SpecialWheel;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class ChooseForMeTest {

    private final ChooseForMe chooseForMe = new ChooseForMe();

    @Test
    public void regularWheel() throws IOException {
        List<String> lines = readLines("test1.in");
        List<Card> cards = readCards(lines, 2);

        List<Card> result = chooseForMe.findCards(cards, readRegularWheel(lines.get(0)), readSpecialWheel(lines.get(1)));

        assertEquals(new HashSet<>(result), new HashSet<>(readCards(readLines("test1.out"), 0)));
    }

    @Test
    public void regularAndSpecialWheel() throws IOException {
        List<String> lines = readLines("test2.in");
        List<Card> cards = readCards(lines, 2);

        List<Card> result = chooseForMe.findCards(cards, readRegularWheel(lines.get(0)), readSpecialWheel(lines.get(1)));

        assertEquals(new HashSet<>(readCards(readLines("test2.out"), 0)), new HashSet<>(result));
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
            int rarity = Integer.parseInt(values[1]);
            CardType cardType = CardType.valueOf(values[2]);
            int count = Integer.parseInt(values[3]);
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
        return new SpecialWheel(Integer.parseInt(line));
    }
}