package temp.okulyk.wos.chooseforme.model.card;

import lombok.Value;

@Value
public class Card {
    String cardName;
    int price;
    CardType cardType;
    int duplicateNumber;
}
