package de.bestellsystem.mahnwesen.application;

import static org.assertj.core.api.Assertions.assertThat;

import de.bestellsystem.TestcontainersKonfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;

/** US-06.2 AK4 mit der Standardkonfiguration des Zeitplans. */
@SpringBootTest(properties = "bestellsystem.mahnlauf.cron=0 0 6 * * *")
@Import(TestcontainersKonfiguration.class)
class MahnlaufZeitplanIT {

    @Autowired private ScheduledTaskHolder zeitplan;

    @Test
    @DisplayName("US-06.2 AK4: Mahnlauf täglich um 06:00 eingeplant")
    void taeglichEingeplant() {
        assertThat(zeitplan.getScheduledTasks())
                .map(ScheduledTask::getTask)
                .singleElement()
                .isInstanceOfSatisfying(
                        CronTask.class,
                        aufgabe -> {
                            assertThat(aufgabe.getExpression()).isEqualTo("0 0 6 * * *");
                            assertThat(aufgabe.toString()).contains("Mahnlauf.fuehreHeuteDurch");
                        });
    }
}
