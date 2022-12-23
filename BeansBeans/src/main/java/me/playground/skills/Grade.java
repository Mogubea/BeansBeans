package me.playground.skills;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Grade {

    INVALID("Unknown", -1),

    F_MINUS("F-", 0),
    F("F", 1),
    F_PLUS("F+", 2),
    E_MINUS("E-", 3),
    E("E", 4),
    E_PLUS("E+", 5),
    D_MINUS("D-", 6),
    D("D", 7),
    D_PLUS("D+", 8),
    C_MINUS("C-", 9),
    C("C", 10),
    C_PLUS("C+", 11),
    B_MINUS("B-", 12),
    B("B", 13),
    B_PLUS("B+", 14),
    A_MINUS("A-", 15),
    A("A", 16),
    A_PLUS("A+", 17),
    S_MINUS("S-", 18),
    S("S", 19),
    S_PLUS("S+", 20);

    private static final Map<String, Grade> fromString = new HashMap<>();
    private static final List<Grade> fromIdx = new ArrayList<>();

    static {
        for (Grade grade : values()) {
            fromString.put(grade.toString(), grade);
            if (grade.gradeIdx < 0) continue;
            fromIdx.add(grade.gradeIdx, grade);
        }
    }

    private final String grade;
    private final int gradeIdx;

    Grade(@NotNull String grade, int gradeIdx) {
        this.grade = grade;
        this.gradeIdx = gradeIdx;
    }

    public int getLevel() {
        return gradeIdx;
    }

    @NotNull
    public String toString() {
        return grade;
    }

    @NotNull
    public static Grade fromString(@NotNull String grade) {
        return fromString.getOrDefault(grade, F_MINUS);
    }

    @NotNull
    public static Grade fromLevel(int level) {
        Grade grade = fromIdx.get(level);

        return grade == null ? INVALID : grade;
    }

}
