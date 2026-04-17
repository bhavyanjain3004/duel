package com.duel.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wall implements Serializable {
    private Position p1;
    private Position p2;

    public void normalize() {
        if (p1.getX() > p2.getX() || (p1.getX() == p2.getX() && p1.getY() > p2.getY())) {
            Position temp = p1;
            p1 = p2;
            p2 = temp;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wall wall = (Wall) o;
        Wall w1 = new Wall(this.p1, this.p2);
        w1.normalize();
        Wall w2 = new Wall(wall.p1, wall.p2);
        w2.normalize();
        return Objects.equals(w1.p1, w2.p1) && Objects.equals(w1.p2, w2.p2);
    }

    @Override
    public int hashCode() {
        Wall w = new Wall(this.p1, this.p2);
        w.normalize();
        return Objects.hash(w.p1, w.p2);
    }
}
