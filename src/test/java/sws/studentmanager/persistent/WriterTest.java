package sws.studentmanager.persistent;

import org.junit.Before;
import org.junit.Test;
import sws.studentmanager.model.Student;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class WriterTest {

    private Student[] students = new Student[1];
    private String studentsJson;


    @Before
    public void setUp() throws Exception {
        students[0] = new Student("Daniel van Wichen", LocalDate.of(1994, 9, 25));

        java.io.Reader reader = new InputStreamReader(getClass().getResourceAsStream("/students.json"));
        Scanner scanner = new Scanner(reader);

        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine()).append("\n");
        }

        studentsJson = sb.toString();
        scanner.close();
    }

    @Test
    public void testWriteStudents() throws Exception {
        File tmpFile = File.createTempFile("students", "json");
        Writer.writeStudents(students, tmpFile.getAbsolutePath());

        BufferedReader br = new BufferedReader(new FileReader(tmpFile.getAbsolutePath()));
        Scanner scanner = new Scanner(br);

        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine()).append("\n");
        }

        String tmpStudentJson = sb.toString();
        scanner.close();

        assertEquals(studentsJson, tmpStudentJson);
    }
}