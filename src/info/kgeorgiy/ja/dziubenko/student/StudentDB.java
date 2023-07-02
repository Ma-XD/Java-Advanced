package info.kgeorgiy.ja.dziubenko.student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class StudentDB implements StudentQuery {

    private static final Comparator<Student> STUDENT_NAME_COMPARATOR =
            Comparator.comparing(Student::getLastName, Comparator.reverseOrder())
                    .thenComparing(Student::getFirstName, Comparator.reverseOrder())
                    .thenComparing(Student::compareTo);

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getFieldsBy(Student::getFirstName, students);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getFieldsBy(Student::getLastName, students);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getFieldsBy(Student::getGroup, students);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getFieldsBy(StudentDB::getFullName, students);
    }

    private static <T> List<T> getFieldsBy(Function<Student, T> studentGetField, List<Student> students) {
        return students.stream()
                .map(studentGetField)
                .toList();
    }

    private static String getFullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortBy(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortBy(students, STUDENT_NAME_COMPARATOR);
    }

    private static List<Student> sortBy(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream()
                .sorted(comparator)
                .toList();
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsBy(Student::getFirstName, students, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsBy(Student::getLastName, students, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentsBy(Student::getGroup, students, group);
    }

    public static <T> List<Student> findStudentsBy(Function<Student, T> studentGetField, Collection<Student> students, T equalTo) {
        return filterStudentsStreamByEqualTo(studentGetField, students, equalTo)
                .sorted(STUDENT_NAME_COMPARATOR)
                .toList();
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return filterStudentsStreamByEqualTo(Student::getGroup, students, group)
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())
                ));
    }

    private static <T> Stream<Student> filterStudentsStreamByEqualTo(Function<Student, T> studentGetField, Collection<Student> students, T equalTo) {
        return students.stream()
                .filter(student -> studentGetField.apply(student).equals(equalTo));
    }
}
