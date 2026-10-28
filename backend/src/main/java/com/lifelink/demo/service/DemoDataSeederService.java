package com.lifelink.demo.service;

import com.lifelink.facility.model.Facility;
import com.lifelink.facility.model.enums.FacilityStatus;
import com.lifelink.facility.model.enums.FacilityType;
import com.lifelink.facility.repository.FacilityRepository;
import com.lifelink.inventory.model.BloodInventoryBatch;
import com.lifelink.inventory.model.InventoryTransaction;
import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import com.lifelink.inventory.model.enums.TransactionType;
import com.lifelink.inventory.repository.BloodInventoryBatchRepository;
import com.lifelink.inventory.repository.InventoryTransactionRepository;
import com.lifelink.request.model.BloodRequest;
import com.lifelink.request.model.BloodRequestResponse;
import com.lifelink.request.model.enums.BankResponseStatus;
import com.lifelink.request.model.enums.RequestStatus;
import com.lifelink.request.model.enums.RequestUrgency;
import com.lifelink.request.repository.BloodRequestRepository;
import com.lifelink.request.repository.BloodRequestResponseRepository;
import com.lifelink.reservation.model.StockReservation;
import com.lifelink.reservation.repository.StockReservationRepository;
import com.lifelink.transfer.model.BloodTransfer;
import com.lifelink.transfer.model.enums.TransferStatus;
import com.lifelink.transfer.repository.BloodTransferRepository;
import com.lifelink.user.model.User;
import com.lifelink.user.model.enums.UserRole;
import com.lifelink.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service providing secure bootstrap of initial network administrator
 * and opt-in simulated demonstration data.
 */
@Service
public class DemoDataSeederService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeederService.class);

    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final BloodInventoryBatchRepository batchRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final BloodRequestRepository requestRepository;
    private final BloodRequestResponseRepository responseRepository;
    private final StockReservationRepository reservationRepository;
    private final BloodTransferRepository transferRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.enabled:true}")
    private boolean bootstrapAdminEnabled;

    @Value("${app.bootstrap-admin.email:admin@lifelink.network}")
    private String adminEmail;

    @Value("${app.bootstrap-admin.password:Admin@LifeLink2026!}")
    private String adminPassword;

    @Value("${app.bootstrap-admin.name:System Network Administrator}")
    private String adminName;

    public DemoDataSeederService(UserRepository userRepository,
                                 FacilityRepository facilityRepository,
                                 BloodInventoryBatchRepository batchRepository,
                                 InventoryTransactionRepository transactionRepository,
                                 BloodRequestRepository requestRepository,
                                 BloodRequestResponseRepository responseRepository,
                                 StockReservationRepository reservationRepository,
                                 BloodTransferRepository transferRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;
        this.batchRepository = batchRepository;
        this.transactionRepository = transactionRepository;
        this.requestRepository = requestRepository;
        this.responseRepository = responseRepository;
        this.reservationRepository = reservationRepository;
        this.transferRepository = transferRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        bootstrapInitialAdminIfMissing();
    }

    @Transactional
    public void bootstrapInitialAdminIfMissing() {
        if (!bootstrapAdminEnabled) {
            return;
        }

        if (userRepository.findByEmail(adminEmail.trim().toLowerCase()).isEmpty()) {
            User admin = new User(
                    adminEmail.trim().toLowerCase(),
                    passwordEncoder.encode(adminPassword),
                    adminName,
                    "+91 9876543210",
                    UserRole.NETWORK_ADMIN,
                    null,
                    "Chief Network Controller"
            );
            userRepository.save(admin);
            log.info("Initialized secure bootstrap administrator account [{}]", adminEmail);
        }
    }

    @Transactional
    public String seedSimulatedDemoNetwork() {
        if (facilityRepository.count() > 2) {
            return "Demo network already initialized. Skipping to preserve existing data.";
        }

        Instant now = Instant.now();

        // 1. Create Fictional Hospital
        Facility hospital = new Facility(
                "Metro City General Hospital (DEMO)",
                "LIC-HOSP-MCGH-2026",
                FacilityType.HOSPITAL,
                "104 Health Boulevard, Sector 12",
                "Chennai",
                "Tamil Nadu",
                "600001",
                "+91 44 2836 1001",
                "coordination@metrohospital.demo"
        );
        hospital.approve("Verified accredited trauma center (Demo)");
        Facility savedHospital = facilityRepository.save(hospital);

        User hospitalUser = new User(
                "hospital@lifelink.demo",
                passwordEncoder.encode("Demo@Hospital2026!"),
                "Dr. Rajesh Sharma (Demo)",
                "+91 98401 23456",
                UserRole.HOSPITAL_USER,
                savedHospital,
                "Head of Emergency Medicine"
        );
        User savedHospitalUser = userRepository.save(hospitalUser);

        // 2. Create Fictional Blood Bank A
        Facility bankA = new Facility(
                "Red Cross Central Blood Center (DEMO)",
                "LIC-BB-RCCB-2026",
                FacilityType.BLOOD_BANK,
                "45 Red Cross Road, Egmore",
                "Chennai",
                "Tamil Nadu",
                "600008",
                "+91 44 2855 4321",
                "contact@redcrosscentral.demo"
        );
        bankA.approve("Certified regional hub blood center (Demo)");
        Facility savedBankA = facilityRepository.save(bankA);

        User bankUserA = new User(
                "bloodbank@lifelink.demo",
                passwordEncoder.encode("Demo@BloodBank2026!"),
                "Kavitha Raman (Demo)",
                "+91 98402 34567",
                UserRole.BLOOD_BANK_USER,
                savedBankA,
                "Chief Blood Transfusion Officer"
        );
        User savedBankUserA = userRepository.save(bankUserA);

        // 3. Create Fictional Blood Bank B
        Facility bankB = new Facility(
                "Apex Life Blood Institute (DEMO)",
                "LIC-BB-ALBI-2026",
                FacilityType.BLOOD_BANK,
                "88 Anna Salai, Guindy",
                "Chennai",
                "Tamil Nadu",
                "600032",
                "+91 44 2235 7890",
                "dispatch@apexbio.demo"
        );
        bankB.approve("Licensed secondary blood bank (Demo)");
        Facility savedBankB = facilityRepository.save(bankB);

        // 4. Create Fictional Blood Bank C
        Facility bankC = new Facility(
                "Mercy City Blood Bank (DEMO)",
                "LIC-BB-MCBB-2026",
                FacilityType.BLOOD_BANK,
                "12 Gandhi Irwin Road",
                "Chennai",
                "Tamil Nadu",
                "600003",
                "+91 44 2561 0022",
                "info@mercycitybb.demo"
        );
        bankC.approve("Licensed community blood bank (Demo)");
        Facility savedBankC = facilityRepository.save(bankC);

        // 5. Seed Inventory Batches in Bank A
        // A+ PRBC (12 units, 25 days left)
        BloodInventoryBatch b1 = new BloodInventoryBatch(
                savedBankA, "BATCH-A1-2026", BloodGroup.A_POSITIVE, ComponentType.RED_BLOOD_CELLS,
                12, now.minus(5, ChronoUnit.DAYS), now.plus(25, ChronoUnit.DAYS), "Refrigerated Rack 01", "Simulated demo stock"
        );
        BloodInventoryBatch savedB1 = batchRepository.save(b1);
        transactionRepository.save(new InventoryTransaction(savedB1, savedBankA, TransactionType.COLLECTION, 12, 12, "DEMO_SEED", savedB1.getId(), savedBankUserA, "Initial demo stock"));

        // O- PRBC (Universal Donor - 6 units, 18 days left)
        BloodInventoryBatch b2 = new BloodInventoryBatch(
                savedBankA, "BATCH-O1-2026", BloodGroup.O_NEGATIVE, ComponentType.RED_BLOOD_CELLS,
                6, now.minus(7, ChronoUnit.DAYS), now.plus(18, ChronoUnit.DAYS), "Refrigerated Rack 02", "Universal donor stock (Demo)"
        );
        BloodInventoryBatch savedB2 = batchRepository.save(b2);
        transactionRepository.save(new InventoryTransaction(savedB2, savedBankA, TransactionType.COLLECTION, 6, 6, "DEMO_SEED", savedB2.getId(), savedBankUserA, "Initial demo stock"));

        // Near-Expiry Platelets (4 units, 3 days left!)
        BloodInventoryBatch b3 = new BloodInventoryBatch(
                savedBankA, "BATCH-PLT-EXP-2026", BloodGroup.B_POSITIVE, ComponentType.PLATELETS,
                4, now.minus(2, ChronoUnit.DAYS), now.plus(3, ChronoUnit.DAYS), "Agitator Shelf 01", "Near-expiry simulated batch"
        );
        batchRepository.save(b3);

        // Quarantined Batch
        BloodInventoryBatch b4 = new BloodInventoryBatch(
                savedBankA, "BATCH-QNT-2026", BloodGroup.AB_POSITIVE, ComponentType.FRESH_FROZEN_PLASMA,
                5, now.minus(10, ChronoUnit.DAYS), now.plus(180, ChronoUnit.DAYS), "Deep Freezer -30C", "Quarantined for confirmatory testing (Demo)"
        );
        b4.quarantine("Routine sample serology test pending verification");
        batchRepository.save(b4);

        // 6. Seed Batches in Bank B
        // A+ PRBC (8 units)
        BloodInventoryBatch b5 = new BloodInventoryBatch(
                savedBankB, "BATCH-B1-2026", BloodGroup.A_POSITIVE, ComponentType.RED_BLOOD_CELLS,
                8, now.minus(4, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), "Rack B-01", "Simulated demo stock"
        );
        batchRepository.save(b5);

        // B+ PRBC (10 units)
        BloodInventoryBatch b6 = new BloodInventoryBatch(
                savedBankB, "BATCH-B2-2026", BloodGroup.B_POSITIVE, ComponentType.RED_BLOOD_CELLS,
                10, now.minus(3, ChronoUnit.DAYS), now.plus(28, ChronoUnit.DAYS), "Rack B-02", "Simulated demo stock"
        );
        batchRepository.save(b6);

        // 7. Seed Batches in Bank C
        // O+ PRBC (15 units)
        BloodInventoryBatch b7 = new BloodInventoryBatch(
                savedBankC, "BATCH-C1-2026", BloodGroup.O_POSITIVE, ComponentType.RED_BLOOD_CELLS,
                15, now.minus(2, ChronoUnit.DAYS), now.plus(35, ChronoUnit.DAYS), "Rack C-01", "Simulated demo stock"
        );
        batchRepository.save(b7);

        // 8. Create a Sample Active Request and Transfer
        BloodRequest sampleReq = new BloodRequest(
                "REQ-2026-DEMO-001",
                savedHospital,
                savedHospitalUser,
                BloodGroup.A_POSITIVE,
                ComponentType.RED_BLOOD_CELLS,
                4,
                RequestUrgency.URGENT,
                now.plus(6, ChronoUnit.HOURS),
                "PAT-CARD-8891",
                "Emergency cardiac surgery replacement (Simulated Demo)"
        );
        sampleReq.transitionStatus(RequestStatus.IN_TRANSFER);
        sampleReq.recordFulfillment(4);
        BloodRequest savedReq = requestRepository.save(sampleReq);

        // Response from Bank A
        BloodRequestResponse respA = new BloodRequestResponse(savedReq, savedBankA);
        respA.accept(4, savedBankUserA, "Units prepared and packaged with cold chain packaging.");
        responseRepository.save(respA);

        // Reservation
        StockReservation res = new StockReservation(
                "RES-DEMO-A1",
                savedReq,
                savedB1,
                savedBankA,
                4,
                now.plus(24, ChronoUnit.HOURS)
        );
        res.markDispatched();
        StockReservation savedRes = reservationRepository.save(res);

        // Transfer
        BloodTransfer transfer = new BloodTransfer(
                "TRF-DEMO-001",
                savedReq,
                savedRes,
                savedBankA,
                savedHospital,
                4
        );
        transfer.dispatch(savedBankUserA, "LifeLink Dedicated Courier Unit 4", "Validated temperature: 3.8°C", "Urgent dispatch");
        transferRepository.save(transfer);

        log.info("Simulated demo healthcare network seeded successfully with 1 Hospital and 3 Blood Banks.");
        return "Simulated demo network seeded successfully!";
    }
}
