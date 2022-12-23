package me.playground.skills;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillTree<T extends Skill> {

    private static final Map<Skill, SkillTree<?>> TREES = new HashMap<>();

    protected final static byte MAX_ROW = 6;
    protected final static byte MAX_COLUMN = 6;

    private final T skill;
    private final List<SkillTreeEntry<T>> entries = new ArrayList<>((MAX_ROW + 1) * (MAX_COLUMN + 1));
    private int cumulativeLevel;

    private SkillTree(T clazz) {
        skill = clazz;
    }

    @NotNull
    protected static <T extends Skill> SkillTree<T> getSkillTree(@NotNull T skill) {
        SkillTree<?> tree = TREES.get(skill);
        if (tree == null) {
            tree = new SkillTree<>(skill);
            TREES.put(skill, tree);
        }

        return (SkillTree<T>) tree;
    }

    /**
     * Add the newly defined Skill Tree Entry to this tree at the specified position.
     * @param row The row this entry will show up on, minimum of 0 and a maximum of 6.
     * @param column The column this entry will show up on, minimum of 0 and a maximum of 6.
     * @param entry The skill tree entry.
     * @throws IllegalArgumentException If providing an illegal row or column value.
     * @throws UnsupportedOperationException If there is already a Skill Tree Entry in the specified row and column.
     */
    protected void addEntry(int row, int column, @NotNull SkillTreeEntry<T> entry) {
        if (row < 0 || row > MAX_ROW) throw new IllegalArgumentException("Row cannot be smaller than 0 or larger than 6.");
        if (column < 0 || column > MAX_ROW) throw new IllegalArgumentException("Column cannot be smaller than 0 or larger than 6.");

        int position = (row + 1) * (column + 1) - 1;
        if (entries.contains(entry) || entries.get(position) != null) throw new UnsupportedOperationException("Skill Tree slot already has an entry.");
        entries.set(position, entry);
        cumulativeLevel += entry.getMaximumLevel();
    }

    @Nullable
    public SkillTreeEntry<T> getEntry(int row, int column) {
        int position = (row + 1) * (column + 1) - 1;
        return entries.get(position);
    }

    protected List<SkillTreeEntry<?>> getEntries() {
       return List.copyOf(entries);
    }

    public int getCumulativeLevel() {
        return cumulativeLevel;
    }

    public int getNextCost(PlayerSkillData skillData, Skill skill) {
        return getCost(skillData.getCumulativePerkLevel(skill));
    }

    public int getCost(int perkIdx) {
        return perkIdx * 5;
    }

}
