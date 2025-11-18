import java.sql.Timestamp;

/**
 * ChangeLog model class representing product change tracking records
 * for audit trails in the marketplace system.
 */
public class ChangeLog {
    private int logId;
    private int productId;
    private int userId;
    private String changeType;
    private String oldValue;
    private String newValue;
    private Timestamp changeDate;
    
    /**
     * Default constructor
     */
    public ChangeLog() {
    }
    
    /**
     * Constructor with all fields except logId (for new records)
     */
    public ChangeLog(int productId, int userId, String changeType, 
                     String oldValue, String newValue, Timestamp changeDate) {
        this.productId = productId;
        this.userId = userId;
        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeDate = changeDate;
    }
    
    /**
     * Constructor with all fields including logId (for existing records)
     */
    public ChangeLog(int logId, int productId, int userId, String changeType,
                     String oldValue, String newValue, Timestamp changeDate) {
        this.logId = logId;
        this.productId = productId;
        this.userId = userId;
        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeDate = changeDate;
    }
    
    // Getters and Setters
    
    public int getLogId() {
        return logId;
    }
    
    public void setLogId(int logId) {
        this.logId = logId;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getChangeType() {
        return changeType;
    }
    
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }
    
    public String getOldValue() {
        return oldValue;
    }
    
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
    
    public String getNewValue() {
        return newValue;
    }
    
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
    
    public Timestamp getChangeDate() {
        return changeDate;
    }
    
    public void setChangeDate(Timestamp changeDate) {
        this.changeDate = changeDate;
    }
    
    @Override
    public String toString() {
        return String.format("ChangeLog[logId=%d, productId=%d, userId=%d, changeType=%s, " +
                           "oldValue=%s, newValue=%s, changeDate=%s]",
                           logId, productId, userId, changeType, oldValue, newValue, changeDate);
    }
}
