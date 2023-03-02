package me.jounhee.chapter02.item10.composition;


import me.jounhee.chapter02.item10.Color;
import me.jounhee.chapter02.item10.Point;

import java.util.Objects;

// 코드 10-5 equals 규약을 지키면서 값 추가하기 (60쪽)
public class ColorPoint {
    private final Point point;
    private final Color color;

    public ColorPoint(int x, int y, Color color) {
        point = new Point(x, y);
        this.color = Objects.requireNonNull(color);
    }

    /**
     * 이 ColorPoint의 Point 뷰를 반환한다.
     */
    public Point asPoint() {
        return point;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorPoint that = (ColorPoint) o;
        return point.equals(that.point) && color == that.color;
    }

    @Override public int hashCode() {
        return 31 * point.hashCode() + color.hashCode();
    }
}
