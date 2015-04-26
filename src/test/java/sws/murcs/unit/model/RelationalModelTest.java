package sws.murcs.unit.model;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import sws.murcs.exceptions.DuplicateObjectException;
import sws.murcs.model.*;
import sws.murcs.debug.sampledata.Generator;
import sws.murcs.debug.sampledata.PersonGenerator;
import sws.murcs.debug.sampledata.SkillGenerator;
import sws.murcs.debug.sampledata.TeamGenerator;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

public class RelationalModelTest {

    private static Generator<Team> teamGenerator;
    private static Generator<Person> personGenerator;
    private static Generator<Skill> skillGenerator;
    private Team teamGenerated;
    private Team team;
    private Person unassignedPerson;
    private Person unassignedPersonGenerated;
    private Skill skillGenerated;
    private Skill skill;
    private RelationalModel relationalModel;
    private Project project;

    @BeforeClass
    public static void oneTimeSetUp() {
        String[] skills = {"skill1", "skill2", "skill3"};
        String[] descriptions = {"description1", "description2", "description3"};
        String[] teamNames = {"name1", "name2", "name3"};

        teamGenerator = new TeamGenerator(personGenerator, teamNames, descriptions, 0.5f, 0.5f);
        personGenerator = new PersonGenerator(skillGenerator);
        skillGenerator = new SkillGenerator(skills, descriptions);
    }

    @Before
    public void setUp() {
        try {
            teamGenerated = teamGenerator.generate();
            unassignedPersonGenerated = personGenerator.generate();
            skillGenerated = skillGenerator.generate();
            unassignedPerson = new Person();
            relationalModel = new RelationalModel();
            team = new Team();
            skill = new Skill();
            project = new Project();
            relationalModel.addProject(project);
        }
        catch (DuplicateObjectException exception) {
            // Ignored
        }
    }

    @After
    public void tearDown() {
        teamGenerated = null;
        relationalModel = null;
        unassignedPerson = null;
        unassignedPersonGenerated = null;
        team = null;
        skill = null;
        skillGenerated = null;
    }

    @Test
    public void addUnassignedPersonTest() throws Exception {
        assertFalse(relationalModel.getUnassignedPeople().contains(unassignedPersonGenerated));

        relationalModel.addPerson(unassignedPersonGenerated);
        assertTrue(relationalModel.getUnassignedPeople().contains(unassignedPersonGenerated));
    }

    @Test (expected = DuplicateObjectException.class)
    public void addUnassignedPersonExceptionTest1() throws Exception {
        relationalModel.addPerson(unassignedPersonGenerated);
        relationalModel.addPerson(unassignedPersonGenerated);
    }

    @Test (expected = DuplicateObjectException.class)
    public void addUnassignedPersonExceptionTest2() throws Exception {
        relationalModel.addPerson(unassignedPersonGenerated);
        unassignedPerson.setShortName(unassignedPersonGenerated.getShortName());
        relationalModel.addPerson(unassignedPerson);
    }

    @Test
    public void addUnassignedPeopleTest() throws Exception {
        List<Person> testUnassignedPeople = new ArrayList<>();
        assertEquals(relationalModel.getUnassignedPeople().size(), 0);
        testUnassignedPeople.add(unassignedPersonGenerated);

        relationalModel.addPeople(testUnassignedPeople);
        assertTrue(relationalModel.getUnassignedPeople().contains(unassignedPersonGenerated));

        testUnassignedPeople.add(unassignedPersonGenerated);
        assertEquals(testUnassignedPeople.size(), 2);
        assertEquals(relationalModel.getUnassignedPeople().size(), 1);
    }

    @Test
    public void removeUnassignedPersonTest() throws Exception {
        relationalModel.addPerson(unassignedPersonGenerated);
        assertTrue(relationalModel.getUnassignedPeople().contains(unassignedPersonGenerated));

        relationalModel.removePerson(unassignedPersonGenerated);
        assertFalse(relationalModel.getUnassignedPeople().contains(unassignedPersonGenerated));

        relationalModel.removePerson(unassignedPersonGenerated);
        assertFalse(relationalModel.getUnassignedPeople().contains(unassignedPersonGenerated));
    }

    @Test
    public void addTeamTest() throws Exception {
        assertFalse(relationalModel.getTeams().contains(teamGenerated));

        relationalModel.addTeam(teamGenerated);
        assertTrue(relationalModel.getTeams().contains(teamGenerated));
    }

    @Test (expected = DuplicateObjectException.class)
    public void addTeamExceptionTest1() throws Exception {
        relationalModel.addTeam(teamGenerated);
        relationalModel.addTeam(teamGenerated);
    }

    @Test (expected = DuplicateObjectException.class)
    public void addTeamExceptionTest2() throws Exception {
        relationalModel.addTeam(teamGenerated);
        team.setShortName(teamGenerated.getShortName());
        relationalModel.addTeam(teamGenerated);
    }

    @Test
    public void addTeamsTest() throws Exception {
        List<Team> testTeams = new ArrayList<>();
        assertEquals(relationalModel.getTeams().size(), 0);
        testTeams.add(teamGenerated);

        relationalModel.addTeams(testTeams);
        assertTrue(relationalModel.getTeams().contains(teamGenerated));

        testTeams.add(teamGenerated);
        assertEquals(testTeams.size(), 2);
        assertEquals(relationalModel.getTeams().size(), 1);
    }

    @Test
    public void removeTeamTest() throws Exception {
        relationalModel.addTeam(teamGenerated);
        assertTrue(relationalModel.getTeams().contains(teamGenerated));

        relationalModel.removeTeam(teamGenerated);
        assertFalse(relationalModel.getTeams().contains(teamGenerated));

        relationalModel.removeTeam(teamGenerated);
        assertFalse(relationalModel.getTeams().contains(teamGenerated));
    }

    @Test
    public void addSkillTest() throws Exception {
        assertFalse(relationalModel.getSkills().contains(skillGenerated));

        relationalModel.addSkill(skillGenerated);
        assertTrue(relationalModel.getSkills().contains(skillGenerated));
    }

    @Test (expected = DuplicateObjectException.class)
    public void addSkillExceptionTest1() throws Exception {
        relationalModel.addSkill(skillGenerated);
        relationalModel.addSkill(skillGenerated);
    }

    @Test (expected = DuplicateObjectException.class)
    public void addSkillExceptionTest2() throws Exception {
        relationalModel.addSkill(skillGenerated);
        skill.setShortName(skillGenerated.getShortName());
        relationalModel.addSkill(skill);
    }

    @Test
    public void addSkillsTest() throws Exception {
        List<Skill> testSkills = new ArrayList<>();
        assertEquals(relationalModel.getSkills().size(), 2);
        testSkills.add(skillGenerated);

        relationalModel.addSkills(testSkills);
        assertTrue(relationalModel.getSkills().contains(skillGenerated));

        testSkills.add(skillGenerated);
        assertEquals(testSkills.size(), 2);
        assertEquals(relationalModel.getSkills().size(), 3);
    }

    @Test
    public void removeSkillTest() throws Exception {
        relationalModel.addSkill(skillGenerated);
        assertTrue(relationalModel.getSkills().contains(skillGenerated));

        relationalModel.removeSkill(skillGenerated);
        assertFalse(relationalModel.getSkills().contains(skillGenerated));

        relationalModel.removeSkill(skillGenerated);
        assertFalse(relationalModel.getSkills().contains(skillGenerated));
    }
}
