/*
 * ESTEAM Software GmbH, 09.12.2007
 */
package org.apache.empire.samples.db;


/**
 * The SampleBean class is used to demonstrate JavaBean support for SQL-Queries.
 * The SampleBean is used in the SampleApp's queryRecords function.
 */
public class SampleBean
{
    private int    employeeId;
    private String fullName;
    private String gender;
    private String phoneNumber;
    private String department;
    private String businessUnit;

    /*
     * Uncomment this if you want to use constructor instead of setters
     * However, number of arguments and data types must match query!
     *
    public SampleBean(int employeeId, String fullName, String gender, String phoneNumber, String department, String businessUnit)
    {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.businessUnit = businessUnit;
    }
    */
    
    public int getEmployeeId()
    {
        return employeeId;
    }

    public void setEmployeeId(int employeeId)
    {
        this.employeeId = employeeId;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getDepartment()
    {
        return department;
    }

    public void setDepartment(String department)
    {
        this.department = department;
    }

    public String getBusinessUnit()
    {
        return businessUnit;
    }

    public void setBusinessUnit(String businessUnit)
    {
        this.businessUnit = businessUnit;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(employeeId);
        buf.append("\t");
        buf.append(fullName);
        buf.append("\t");
        buf.append(gender);
        buf.append("\t");
        buf.append(department);
        buf.append("\t");
        buf.append(businessUnit);
        return buf.toString();
    }
    
}
