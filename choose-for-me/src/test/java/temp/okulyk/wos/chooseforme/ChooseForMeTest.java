package temp.okulyk.wos.chooseforme;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import org.junit.Test;
import temp.okulyk.wos.chooseforme.model.card.Card;
import temp.okulyk.wos.chooseforme.model.card.CardType;
import temp.okulyk.wos.chooseforme.model.wheel.RegularWheel;
import temp.okulyk.wos.chooseforme.model.wheel.SpecialWheel;

import static java.util.Set.of;
import static org.junit.Assert.assertEquals;

public class ChooseForMeTest {

    private final ChooseForMe chooseForMe = new ChooseForMe();

    @Test
    public void findCards() {
        List<Card> cards = readCards("test1.in");

        List<Card> result = chooseForMe.findCards(cards, new RegularWheel(of(22)), new SpecialWheel(0));

        assertEquals(result, readCards("test1.out"));
    }

    @SneakyThrows
    private List<Card>  readCards(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(getClass().getClassLoader().getResource(fileName).getFile()))) {
            List<Card> cards = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(" ");
                String cardName = values[0];
                int rarity = Integer.parseInt(values[1]);
                CardType cardType = CardType.valueOf(values[2]);
                int count = Integer.parseInt(values[3]);
                for (int i = 0; i < count; i++) {
                    cards.add(new Card(cardName, rarity, cardType, i));
                }
            }
            return cards;
        }
    }
}