package Domain;

import java.util.Random;

/**
 * Enumerates the five possible enchantments.
 * For now, we only need them to pick a random type.
 */
public enum EnchantmentType {
    REVEAL,
    CLOAK,
    LURINGGEM,
    EXTRATIME,
    EXTRALIFE;

    /**
     * Returns a random EnchantmentType from the five defined above.
     */
    public static EnchantmentType getRandomType(Random random) {
        EnchantmentType[] values = EnchantmentType.values();
        return values[random.nextInt(values.length)];
    }
}
