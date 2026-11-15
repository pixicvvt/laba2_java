package minidb;

/** Операции сравнения, которые можно писать в WHERE. */
public enum Op {
    /** Равно (=). */
    EQ,
    /** Не равно (!= или &lt;&gt;). */
    NE,
    /** Меньше (&lt;). */
    LT,
    /** Меньше или равно (&lt;=). */
    LE,
    /** Больше (&gt;). */
    GT,
    /** Больше или равно (&gt;=). */
    GE;

    /**
     * Превращает текст знака сравнения в операцию.
     * @param symbol знак: =, !=, &lt;&gt;, &lt;, &lt;=, &gt;, &gt;=
     * @return операция
     * @throws DbException если знак неизвестен
     */
    public static Op fromSymbol(String symbol) {
        if (symbol.equals("=")) return EQ;
        if (symbol.equals("!=") || symbol.equals("<>")) return NE;
        if (symbol.equals("<")) return LT;
        if (symbol.equals("<=")) return LE;
        if (symbol.equals(">")) return GT;
        if (symbol.equals(">=")) return GE;
        throw new DbException("Unknown operator: " + symbol);
    }
}
