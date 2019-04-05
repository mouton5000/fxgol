package editor;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class AliveCircle extends Group {
    int i;
    int j;

    public AliveCircle(double centerX, double centerY, double radius) {
        Circle circle = new Circle(centerX, centerY, radius);
        Rectangle rect = new Rectangle(centerX - radius - 3, centerY - radius - 3, 2 * radius + 6, 2 * radius + 6);
        rect.setFill(Color.TRANSPARENT);

        this.getChildren().add(rect);
        this.getChildren().add(circle);

        this.setOnMouseClicked(mouseEvent -> {
            System.out.println(this);
            ((EditorPane)this.getParent()).getChildren().remove(this);
            mouseEvent.consume();
        });
    }

}
