package temp.okulyk.wos.chooseforme.model.card;

import lombok.Value;

@Value
public class Card {
    String cardName;
    int rarity;
    CardType cardType;
    int duplicateNumber;
}
