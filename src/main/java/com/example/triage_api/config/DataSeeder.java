package com.example.triage_api.config;

import com.example.triage_api.model.*;
import com.example.triage_api.repository.DoctorRepository;
import com.example.triage_api.repository.HealthTipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final HealthTipRepository healthTipRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedHealthTips();
        seedSampleDoctors();
    }

    private void seedHealthTips() {
        if (healthTipRepository.count() > 0) {
            log.info("Health tips already seeded. Skipping.");
            return;
        }
        log.info("Seeding health tips...");

        // GREEN tips
        healthTipRepository.save(HealthTip.builder()
                .title("Stay Hydrated")
                .content("Drink at least 8 glasses of water per day. Dehydration can worsen many mild symptoms including headaches, fatigue, and dizziness.")
                .triageLevel(TriageLevel.GREEN).category("hydration").build());

        healthTipRepository.save(HealthTip.builder()
                .title("Rest and Sleep")
                .content("Allow your body time to recover. Aim for 8-9 hours of sleep. Avoid strenuous activity until you feel fully recovered.")
                .triageLevel(TriageLevel.GREEN).category("rest").build());

        healthTipRepository.save(HealthTip.builder()
                .title("Over-the-Counter Relief")
                .content("Paracetamol (acetaminophen) can help with mild fever and pain. Follow dosage instructions. Do not exceed 4g per day.")
                .triageLevel(TriageLevel.GREEN).category("medication").build());

        healthTipRepository.save(HealthTip.builder()
                .title("Monitor Your Symptoms")
                .content("Keep track of your symptoms over the next 24-48 hours. If they worsen or new symptoms appear, seek medical advice.")
                .triageLevel(TriageLevel.GREEN).category("monitoring").build());

        // AMBER tips
        healthTipRepository.save(HealthTip.builder()
                .title("See a Doctor Within 24 Hours")
                .content("Your symptoms suggest you should be evaluated by a healthcare professional. Visit a clinic or call your doctor today.")
                .triageLevel(TriageLevel.AMBER).category("urgent-care").build());

        healthTipRepository.save(HealthTip.builder()
                .title("Do Not Drive Yourself")
                .content("If you are feeling unwell, arrange for someone to take you to the clinic. Do not drive if you feel dizzy or confused.")
                .triageLevel(TriageLevel.AMBER).category("safety").build());

        healthTipRepository.save(HealthTip.builder()
                .title("Bring a List of Medications")
                .content("Write down all medications you are currently taking (including supplements and traditional remedies) to show the doctor.")
                .triageLevel(TriageLevel.AMBER).category("preparation").build());

        healthTipRepository.save(HealthTip.builder()
                .title("Stay Hydrated While You Wait")
                .content("While waiting for your appointment, continue drinking fluids. Avoid alcohol and caffeine as they can worsen dehydration.")
                .triageLevel(TriageLevel.AMBER).category("hydration").build());

        // RED tips
        healthTipRepository.save(HealthTip.builder()
                .title("GO TO THE EMERGENCY ROOM NOW")
                .content("Your symptoms require immediate medical attention. Call emergency services (112 or 119) or go directly to the nearest emergency room. Do not wait.")
                .triageLevel(TriageLevel.RED).category("emergency").build());

        healthTipRepository.save(HealthTip.builder()
                .title("Call Emergency Services")
                .content("If you cannot transport yourself safely, call an ambulance immediately. In Cameroon, you can reach emergency services at 119.")
                .triageLevel(TriageLevel.RED).category("emergency").build());

        healthTipRepository.save(HealthTip.builder()
                .title("Do Not Eat or Drink")
                .content("If emergency surgery might be needed, do not eat or drink anything until a doctor has assessed you.")
                .triageLevel(TriageLevel.RED).category("pre-hospital").build());

        healthTipRepository.save(HealthTip.builder()
                .title("Stay Calm and Keep Someone With You")
                .content("Stay as calm as possible and have someone stay with you until emergency help arrives. Tell them your symptoms and this app's assessment.")
                .triageLevel(TriageLevel.RED).category("safety").build());

        log.info("Health tips seeded. {} tips total.", healthTipRepository.count());
    }

    private void seedSampleDoctors() {
        if (doctorRepository.count() > 0) {
            log.info("Doctors already seeded. Skipping.");
            return;
        }
        log.info("Seeding sample doctors...");

        doctorRepository.save(Doctor.builder()
                .fullName("Dr. Amina Bello")
                .email("amina.bello@triageapp.cm")
                .passwordHash(passwordEncoder.encode("doctor1234"))
                .specialty("General Practice")
                .licenseNumber("CM-MED-2015-00423")
                .bio("10 years of experience in general medicine and family health. Passionate about preventive care and patient education.")
                .yearsExperience(10)
                .languagesSpoken("EN,FR")
                .status(DoctorStatus.AVAILABLE)
                .maxActiveChats(5)
                .build());

        doctorRepository.save(Doctor.builder()
                .fullName("Dr. Jean-Pierre Kamga")
                .email("jean.kamga@triageapp.cm")
                .passwordHash(passwordEncoder.encode("doctor1234"))
                .specialty("Pediatrics")
                .licenseNumber("CM-MED-2018-00871")
                .bio("Specialist in children's health from newborns to adolescents. 6 years of clinical experience in Yaounde Central Hospital.")
                .yearsExperience(6)
                .languagesSpoken("FR,EN")
                .status(DoctorStatus.AVAILABLE)
                .maxActiveChats(4)
                .build());

        doctorRepository.save(Doctor.builder()
                .fullName("Dr. Fatima Nkrumah")
                .email("fatima.nkrumah@triageapp.cm")
                .passwordHash(passwordEncoder.encode("doctor1234"))
                .specialty("Cardiology")
                .licenseNumber("CM-MED-2010-00234")
                .bio("Cardiologist with 15 years of experience. Specialises in hypertension, arrhythmia, and heart failure management.")
                .yearsExperience(15)
                .languagesSpoken("EN,FR,AR")
                .status(DoctorStatus.OFFLINE)
                .maxActiveChats(3)
                .build());

        log.info("Sample doctors seeded. Default password for all: doctor1234");
        log.info("  amina.bello@triageapp.cm   — General Practice (AVAILABLE)");
        log.info("  jean.kamga@triageapp.cm    — Pediatrics (AVAILABLE)");
        log.info("  fatima.nkrumah@triageapp.cm — Cardiology (OFFLINE)");
    }
}
