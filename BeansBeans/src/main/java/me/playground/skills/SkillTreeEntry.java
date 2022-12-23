package me.playground.skills;

import me.playground.items.lore.Lore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class SkillTreeEntry<T extends Skill> {

    private static final Map<String, SkillTreeEntry<?>> identifiers = new HashMap<>();

    // Perks
    public static final SkillTreeEntry<SkillMining> TEST =              register(Skill.MINING, "TEST", "Test Perk", Grade.E, 1, 3);
    public static final SkillTreeEntry<SkillMining> TEST2 =             register(Skill.MINING, "TEST2", "Test Perk", Grade.D, 2, 3).addPrerequisite(TEST, 3).setExpensePrerequisite(5);
    public static final SkillTreeEntry<SkillMining> TEST3 =             register(Skill.MINING, "TEST3", "Test Perk", Grade.E, 1, 4).addPrerequisite(TEST, 3).setMaximumLevel(3);
    public static final SkillTreeEntry<SkillAgriculture> TEST4 =        register(Skill.AGRICULTURE, "TEST", "Test Agriculture Perk", Grade.C, 1, 1);

    // Lore Setting
    static {
        TEST.setLore(Lore.getBuilder("Test Lore for the first &#aaddffMining Skill&r. Test &b{0} &a{1} &d{2}"), (lvl) -> TEST.lore.getLore(0.33f * lvl, 100 * lvl, lvl));
        TEST2.setLore(Lore.getBuilder("Test Lore for the second Mining Skill"));
    }

    private final T skill;
    private final SkillTree<T> tree;
    private final Component displayName;
    private final String databaseIdentifier;
    private final Grade gradeRequirement;

    private Lore lore;
    private Function<Integer, List<TextComponent>> function;

    private final Map<SkillTreeEntry<T>, Byte> prerequisites = new HashMap<>();
    private int expensePrerequisite = 0;
    private byte maxLevel = 10;

    private SkillTreeEntry(SkillTree<T> tree, T skill, String identifier, String displayName, Grade requirement) {
        this.skill = skill;
        this.tree = tree;
        this.databaseIdentifier = identifier;
        this.gradeRequirement = requirement;
        this.lore = Lore.getBuilder("The description for this perk has not been defined yet.").build();
        this.function = (level) -> this.lore.getLore();
        this.displayName = Component.text(displayName, skill.getColour());
    }

    /**
     * Instantiates a {@link SkillTreeEntry} and adds it to the correct {@link SkillTree}.
     */
    private static <T extends Skill> SkillTreeEntry<T> register(@NotNull T skill, @NotNull String identifier, @NotNull String name, @NotNull Grade requirement, int row, int column) {
        if (identifiers.containsKey(identifier)) throw new UnsupportedOperationException("The identifier '" + identifier + "' is already in use!");

        SkillTree<T> tree = SkillTree.getSkillTree(skill);
        SkillTreeEntry<T> entry = new SkillTreeEntry<>(tree, skill, identifier, name, requirement);

        tree.addEntry(row, column, entry);
        identifiers.put(identifier, entry);
        return entry;
    }

    /**
     * Adds a pre-requisite perk that is required to be unlocked at the provided level before unlocking this one.
     * @param entry The pre-requisite perk
     * @param level The level of the pre-requisite perk
     * @return this
     */
    private SkillTreeEntry<T> addPrerequisite(@NotNull SkillTreeEntry<T> entry, int level) {
        if (level > entry.maxLevel || level < 1) throw new IllegalArgumentException("Invalid perk level prerequisite.");
        prerequisites.put(entry, (byte) level);
        return this;
    }

    /**
     * Sets the lore
     */
    private void setLore(@NotNull Lore.PersistentLoreBuilder lore) {
        this.lore = lore.build();
        this.function = (level) -> this.lore.getLore();
    }

    /**
     * Sets the lore and function for obtaining specified lore.
     */
    private void setLore(@NotNull Lore.PersistentLoreBuilder lore, @Nullable Function<Integer, List<TextComponent>> function) {
        this.lore = lore.build();
        this.function = function == null ? (level) -> this.lore.getLore() : function;
    }

    /**
     * Set the maximum level of this perk.
     * @return this
     */
    private SkillTreeEntry<T> setMaximumLevel(int maximumLevel) {
        if (maximumLevel > Byte.MAX_VALUE) maximumLevel = Byte.MAX_VALUE;
        else if (maximumLevel < 1) maximumLevel = 1;

        this.maxLevel = (byte) maximumLevel;
        return this;
    }

    /**
     * @return The maximum level of this perk.
     */
    public int getMaximumLevel() {
        return maxLevel;
    }

    /**
     * Sets the amount of skill essence that has to have been spent before allowing the unlocking of this perk.
     * @return this
     */
    private SkillTreeEntry<T> setExpensePrerequisite(int amountSpent) {
        this.expensePrerequisite = amountSpent;
        return this;
    }

    /**
     * @return The amount of Skill Essence that has to have been spent before allowing the unlocking of this perk.
     */
    public int getExpensePrerequisite() {
        return expensePrerequisite;
    }

    @NotNull
    public List<TextComponent> getLore(int level) {
        return function.apply(level);
    }

    @NotNull
    public SkillTree<T> getSkillTree() {
        return tree;
    }

    @NotNull
    public Skill getSkill() {
        return skill;
    }

    @Nullable
    public Grade getGradeRequirement() {
        return gradeRequirement;
    }

    @NotNull
    public String getIdentifier() {
        return databaseIdentifier;
    }

    @NotNull
    public Component getDisplayName() {
        return displayName;
    }

    @Nullable
    public static SkillTreeEntry<?> getByName(@NotNull String name) {
        return identifiers.get(name);
    }

}