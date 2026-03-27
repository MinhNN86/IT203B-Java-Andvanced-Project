package model;

public class Room {
    private int roomId;
    private String roomName;
    private int capacity;
    private String location;
    private String fixedEquipment;
    private boolean active;

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFixedEquipment() {
        return fixedEquipment;
    }

    public void setFixedEquipment(String fixedEquipment) {
        this.fixedEquipment = fixedEquipment;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return String.format(
                "Room{id=%d, name='%s', capacity=%d, location='%s', active=%s}",
                roomId,
                roomName,
                capacity,
                location,
                active);
    }
}
