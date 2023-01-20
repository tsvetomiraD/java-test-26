package thymeleaf;

public class Student {
    public int id;
    public String name;
    public char gender;

    public Student(int id, String name, char gender) {
        this.gender = gender;
        this.id = id;
        this.name = name;
    }
}
