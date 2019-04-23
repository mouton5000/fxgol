package util;

public class Coords {
    public int line;
    public int column;

    public Coords(int line, int column) {
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return "(" + this.line + " " + this.column + ")";
    }
}