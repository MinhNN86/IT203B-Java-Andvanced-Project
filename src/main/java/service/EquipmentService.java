package service;

import dao.EquipmentDao;
import model.Equipment;

import java.util.List;
import java.util.Optional;

public class EquipmentService {
    private final EquipmentDao equipmentDao = new EquipmentDao();

    public List<Equipment> getAllEquipments() {
        return equipmentDao.findAll();
    }

    public Equipment getEquipmentById(int equipmentId) {
        return equipmentDao.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay thiet bi voi id = " + equipmentId));
    }

    public void createEquipment(String equipmentName, int totalQuantity, int availableQuantity, String status) {
        validateEquipment(equipmentName, totalQuantity, availableQuantity, status);

        Equipment equipment = new Equipment();
        equipment.setEquipmentName(equipmentName.trim());
        equipment.setTotalQuantity(totalQuantity);
        equipment.setAvailableQuantity(availableQuantity);
        equipment.setStatus(status);

        equipmentDao.create(equipment);
    }

    public void updateEquipment(int equipmentId,
            String equipmentName,
            int totalQuantity,
            int availableQuantity,
            String status) {
        validateEquipment(equipmentName, totalQuantity, availableQuantity, status);

        Optional<Equipment> current = equipmentDao.findById(equipmentId);
        if (current.isEmpty()) {
            throw new IllegalArgumentException("Khong tim thay thiet bi can cap nhat.");
        }

        Equipment equipment = current.get();
        equipment.setEquipmentName(equipmentName.trim());
        equipment.setTotalQuantity(totalQuantity);
        equipment.setAvailableQuantity(availableQuantity);
        equipment.setStatus(status);

        equipmentDao.update(equipment);
    }

    public void deleteEquipment(int equipmentId) {
        if (!equipmentDao.delete(equipmentId)) {
            throw new IllegalArgumentException("Khong tim thay thiet bi de xoa.");
        }
    }

    private void validateEquipment(String equipmentName,
            int totalQuantity,
            int availableQuantity,
            String status) {
        if (equipmentName == null || equipmentName.isBlank()) {
            throw new IllegalArgumentException("Ten thiet bi khong duoc de trong.");
        }
        if (totalQuantity <= 0) {
            throw new IllegalArgumentException("Tong so luong phai lon hon 0.");
        }
        if (availableQuantity < 0 || availableQuantity > totalQuantity) {
            throw new IllegalArgumentException("So luong kha dung phai trong khoang [0, tong so luong].");
        }
        if (!"ACTIVE".equals(status) && !"MAINTENANCE".equals(status) && !"INACTIVE".equals(status)) {
            throw new IllegalArgumentException("Trang thai thiet bi khong hop le.");
        }
    }
}
