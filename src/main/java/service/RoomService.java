package service;

import dao.RoomDao;
import model.Room;

import java.util.List;
import java.util.Optional;

public class RoomService {
    private final RoomDao roomDao = new RoomDao();

    public List<Room> getAllRooms() {
        return roomDao.findAll();
    }

    public Room getRoomById(int roomId) {
        return roomDao.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong voi id = " + roomId));
    }

    public void createRoom(String roomName, int capacity, String location, String fixedEquipment) {
        validateRoom(roomName, capacity);

        Room room = new Room();
        room.setRoomName(roomName.trim());
        room.setCapacity(capacity);
        room.setLocation(location);
        room.setFixedEquipment(fixedEquipment);
        room.setActive(true);

        roomDao.create(room);
    }

    public void updateRoom(int roomId, String roomName, int capacity, String location, String fixedEquipment,
            boolean active) {
        validateRoom(roomName, capacity);

        Optional<Room> current = roomDao.findById(roomId);
        if (current.isEmpty()) {
            throw new IllegalArgumentException("Khong tim thay phong can cap nhat.");
        }

        Room room = current.get();
        room.setRoomName(roomName.trim());
        room.setCapacity(capacity);
        room.setLocation(location);
        room.setFixedEquipment(fixedEquipment);
        room.setActive(active);

        roomDao.update(room);
    }

    public void deactivateRoom(int roomId) {
        if (!roomDao.deactivate(roomId)) {
            throw new IllegalArgumentException("Khong tim thay phong de xoa.");
        }
    }

    private void validateRoom(String roomName, int capacity) {
        if (roomName == null || roomName.isBlank()) {
            throw new IllegalArgumentException("Ten phong khong duoc de trong.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Suc chua phong phai lon hon 0.");
        }
    }
}
