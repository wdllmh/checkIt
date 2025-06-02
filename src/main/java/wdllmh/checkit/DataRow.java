package wdllmh.checkit;

import javafx.beans.property.*;
import javafx.scene.image.Image;


public class DataRow {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty english;
    private final DoubleProperty weight;
    private final ObjectProperty<Image> photo;
    private final StringProperty comment;

    public DataRow() {
        id = new SimpleIntegerProperty();
        name = new SimpleStringProperty();
        english = new SimpleStringProperty();
        weight = new SimpleDoubleProperty();
        photo = new SimpleObjectProperty<>();
        comment = new SimpleStringProperty();
    }

    public DataRow(Integer id, Double weight, String name, String english, Image photo, String comment) {
        this.id = new SimpleIntegerProperty(id);
        this.weight = new SimpleDoubleProperty(weight);
        this.name = new SimpleStringProperty(name);
        this.english = new SimpleStringProperty(english);
        this.photo = new SimpleObjectProperty<>(photo);
        this.comment = new SimpleStringProperty(comment);
    }

    public Integer getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(Integer id) { this.id.set(id); }

    public Double getWeight() {
        return weight.get();
    }

    public DoubleProperty weightProperty() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight.set(weight);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getEnglish() {
        return english.get();
    }

    public StringProperty englishProperty() {
        return english;
    }

    public void setEnglish(String english) {
        this.english.set(english);
    }

    public Image getPhoto() {
        return photo.get();
    }

    public ObjectProperty<Image> photoProperty() {
        return photo;
    }

    public void setPhoto(Image photo) {
        this.photo.set(photo);
    }

    public String getComment() {
        return comment.get();
    }

    public StringProperty commentProperty() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment.set(comment);
    }
}
