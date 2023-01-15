package practise.patients;

import practise.Practise;
import practise.patients.patientFiles.PatientFile;
import practise.patients.patientFiles.PatientFileHandler;
import practise.patients.treatment.Rooms;
import practise.patients.treatment.Treatment;
import practise.patients.treatment.TreatmentType;
import practise.stock.Item;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PatientHandler {
    final PatientFileHandler patientFileHandler = new PatientFileHandler();
    private final ArrayList<TreatmentType> treatments = new ArrayList<>();
    private final Practise practise;
    private ArrayList<Treatment> runningTreatments = new ArrayList<>();

    public PatientHandler(Practise practise) {
        this.practise = practise;
    }

    /**
     * Creates a new type of treatment
     *
     * @param name Name of treatment
     * @param cost Cost of treatment
     */
    public void addTreatmentType(String name, double cost, Map<Item, Integer> needs) throws Exception {
        if (treatments.stream().filter(t -> Objects.equals(t.getName(), name)).toList().size() > 0)
            throw new Exception("Treatment type name already in use");
        treatments.add(new TreatmentType(name, cost, needs));
    }

    /**
     * Removes a type of treatment
     *
     * @param index Treatment index, use viewTreatments to view indexes
     */
    public void removeTreatmentType(int index) {
        treatments.remove(index);
    }

    /**
     * Starts new treatment, closes Room, takes needed items from stock and occupies room
     *
     * @param treatmentIndex Index of treatment type to do
     * @param patientFile    Patient to be treated
     * @param room           Room that the appointment will be in
     */
    public void startTreatment(int treatmentIndex, PatientFile patientFile, Rooms room) throws Exception {
        runningTreatments.add(new Treatment(treatments.get(treatmentIndex), patientFile));
        // takes needed items from stock
        treatments.get(treatmentIndex).getNeeds().forEach((key, val) -> {
            try {
                practise.getStockHandler().take(key, val);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        room.closeRoom();
    }

    /**
     * Ends running treatment, frees up room and increases budget by treatment cost
     *
     * @param index Index of treatment to be ended
     * @param room  Room to be opened after appointment
     */
    public void endTreatment(int index, Rooms room ) {
        practise.increaseBudget(treatments.get(index).getCost());
        runningTreatments.remove(index);
        room.openRoom();
    }

    /**
     * Returns runningTreatments index for specified patient
     *
     * @param patientFile Patient to search
     * @return Index of runningTreatments for use with endTreatment()
     * @throws Exception In case specified patient is not in runningTreatments
     */
    public int getIndex(PatientFile patientFile) throws Exception {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (i >= runningTreatments.size()) {
                throw new Exception("Specified patient not in runningTreatments!");
            } else if (runningTreatments.get(i).getPatientFile() == patientFile) {
                found = true;
            } else {
                i++;
            }
        }
        return i;
    }

    /**
     * Displays all Treatment names and costs with their index
     */
    public void viewTreatmentTypes() {
        int i = 0;
        for (TreatmentType t : treatments) {
            System.out.println(i + ": " + t.getName() + ", " + t.getCost());
            i++;
        }
    }

    /**
     * Displays all running treatments; index, name of patients and the type of treatment they get
     */
    public void viewRunningTreatments() {
        int i = 0;
        for (Treatment t : runningTreatments) {
            practise.logger.info(String.format("%s: %s, %s",
                    (i++), t.getPatientFile().getPatient().getName(), t.getTreatmentType().getName()));
        }
    }

    /**
     * returns TreatmentType by given name
     * @param name name of TreatmentType
     */
    public TreatmentType getTreatmentType(String name) throws Exception {
        TreatmentType t = null;
        for (int i = 0; i < treatments.size(); i++) {
            if (treatments.get(i).getName().trim().equalsIgnoreCase(name.trim())) {
                t = treatments.get(i);
                if (t == null) throw new Exception("Name missing.");
            }
        }
        return t;
    }

    public ArrayList<TreatmentType> getTreatmentTypes() {
        return treatments;
    }

    public ArrayList<Treatment> getRunningTreatments() {
        return runningTreatments;
    }

    public PatientFileHandler getPatientFileHandler() {
        return patientFileHandler;
    }
}