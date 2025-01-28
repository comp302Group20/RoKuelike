package Domain;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Overview: Inventory is a mutable collection that manages a grid-based storage of enchantments.
 * It provides functionality for storing and displaying enchantments in a 3x2 grid layout with spacing.
 *
 * Abstract Function:
 * AF(c) = a grid-based inventory i where
 *   i.items = list of enchantments stored in c.collectedEnchantments
 *   i.layout = 3x2 grid with slot size of 64x64 pixels and 10 pixel spacing
 *   i.maxCapacity = SLOTS_X * SLOTS_Y (6 slots)
 *
 * Representation Invariant:
 * - collectedEnchantments is not null
 * - collectedEnchantments.size() <= SLOTS_X * SLOTS_Y
 * - SLOTS_X, SLOTS_Y, SLOT_SIZE, and SPACING are all positive integers
 * - no element in collectedEnchantments is null
 */
public class InventoryTest {
    private Inventory inventory;
    private static final int CELL_SIZE = 64;

    @Before
    public void setUp() {
        inventory = new Inventory();
    }

    /**
     * Checks if the representation invariant holds
     */
    public boolean repOk(Inventory inv) {
        if (inv.getCollectedEnchantments() == null) {
            return false;
        }

        if (inv.getCollectedEnchantments().size() > Inventory.SLOTS_X * Inventory.SLOTS_Y) {
            return false;
        }

        if (Inventory.SLOTS_X <= 0 || Inventory.SLOTS_Y <= 0 ||
                Inventory.SLOT_SIZE <= 0 || Inventory.SPACING <= 0) {
            return false;
        }

        for (Enchantment e : inv.getCollectedEnchantments()) {
            if (e == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Test Case 1: Adding enchantments up to capacity
     * Requires: Empty inventory
     * Effects: Inventory should accept enchantments up to max capacity
     */
    @Test
    public void testAddEnchantments() {
        // Add enchantments up to capacity
        for (int i = 0; i < Inventory.SLOTS_X * Inventory.SLOTS_Y; i++) {
            Enchantment e = new Enchantment(0, 0, CELL_SIZE, CELL_SIZE, EnchantmentType.REVEAL);
            inventory.addEnchantment(e);
            assertEquals("Size should increase with each addition",
                    i + 1, inventory.getCollectedEnchantments().size());
        }

        // Try adding one more
        Enchantment extra = new Enchantment(0, 0, CELL_SIZE, CELL_SIZE, EnchantmentType.REVEAL);
        inventory.addEnchantment(extra);
        assertEquals("Should not exceed max capacity",
                Inventory.SLOTS_X * Inventory.SLOTS_Y,
                inventory.getCollectedEnchantments().size());
    }

    /**
     * Test Case 2: Clearing enchantments
     * Requires: Inventory with some enchantments
     * Effects: All enchantments should be removed
     */
    @Test
    public void testClearEnchantments() {
        // Add some enchantments
        inventory.addEnchantment(new Enchantment(0, 0, CELL_SIZE, CELL_SIZE, EnchantmentType.REVEAL));
        inventory.addEnchantment(new Enchantment(0, 0, CELL_SIZE, CELL_SIZE, EnchantmentType.CLOAK));

        // Clear and verify
        inventory.clearEnchantments();
        assertTrue("Inventory should be empty after clearing",
                inventory.getCollectedEnchantments().isEmpty());
        assertTrue("Representation invariant should hold after clearing",
                repOk(inventory));
    }

    /**
     * Test Case 3: Setting enchantments list
     * Requires: Valid list of enchantments
     * Effects: Inventory should contain exactly the provided enchantments
     */
    @Test
    public void testSetEnchantments() {
        List<Enchantment> enchantments = new ArrayList<>();
        enchantments.add(new Enchantment(0, 0, CELL_SIZE, CELL_SIZE, EnchantmentType.REVEAL));
        enchantments.add(new Enchantment(0, 0, CELL_SIZE, CELL_SIZE, EnchantmentType.CLOAK));

        inventory.setEnchantments(enchantments);
        assertEquals("Should have exactly the set enchantments",
                enchantments.size(), inventory.getCollectedEnchantments().size());
        assertTrue("Representation invariant should hold after setting",
                repOk(inventory));
    }

    /**
     * Test Case 4: Testing full inventory condition
     * Requires: Empty inventory
     * Effects: isFull should return true only when at capacity
     */
    @Test
    public void testFullInventory() {
        assertFalse("New inventory should not be full", inventory.isFull());

        // Fill inventory
        for (int i = 0; i < Inventory.SLOTS_X * Inventory.SLOTS_Y; i++) {
            inventory.addEnchantment(new Enchantment(0, 0, CELL_SIZE, CELL_SIZE,
                    EnchantmentType.REVEAL));
        }

        assertTrue("Inventory should be full at capacity", inventory.isFull());
        assertTrue("Full inventory should maintain representation invariant",
                repOk(inventory));
    }

    /**
     * Test Case 5: Testing inventory layout constraints
     * Requires: Nothing
     * Effects: Verifies the grid layout constants are valid
     */
    @Test
    public void testLayoutConstraints() {
        assertTrue("Slot dimensions should be positive", Inventory.SLOT_SIZE > 0);
        assertTrue("Grid should have positive dimensions",
                Inventory.SLOTS_X > 0 && Inventory.SLOTS_Y > 0);
        assertTrue("Spacing should be positive", Inventory.SPACING > 0);
        assertEquals("Grid should have correct capacity",
                6, Inventory.SLOTS_X * Inventory.SLOTS_Y);
    }
}