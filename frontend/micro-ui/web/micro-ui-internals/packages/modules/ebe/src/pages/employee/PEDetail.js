import { CardLabel, DatePicker, Dropdown, LabelFieldPair, TextInput, TextArea, Toast, Loader } from "@upyog/digit-ui-react-components";
import isEqual from "lodash.isequal";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import CustomTable from "../../components/customTable";

const PreDetail = (defaultValues) => {
    const { t } = useTranslation();
    const [data, setData] = useState([]);
    const [totalRecords, setTotalRecords] = useState(0);
    const [sortParams, setSortParams] = useState({});

    // Set the value once when the component mounts

    const initialData = sessionStorage.getItem("WorkingID");
    const { data: test, isLoading: isEmployeeLoading } = Digit.Hooks.ebe.useGetEnquiry({"enqId":initialData,type:0}, true);

    useEffect(() => {
        if (!isEmployeeLoading && test) {
            const newData = processdata(test.peEnquiryList);
            setData(transformEmployeeData(newData[0])); 
        }
    }, [isEmployeeLoading, test]);

    function processdata(test1) {
        return test1.map((ele) => {
            return ele.data2;
        });
    }

    const {control,setValue} = useForm();

    useEffect(() => {
        if (!isEmployeeLoading && test) {
            const details = test?.peEnquiryList[0].data1 || {};
            console.log(details);
            if (details) {
                setValue("enquiryCode", details.enquiryCode);
                setValue("oldenquiryCode", details.oldenquiryCode);
                setValue("orderNo", details.orderNo);
                setValue("employeename", details.employeeName);
                setValue("orderDate", details.orderDate);
                setValue("enquirySubject", details.enquirySubject);
                setValue("employee", details.employee);
                setValue("department", t("COMMON_MASTERS_DEPARTMENT_" + details.department.i18key));
                setValue("designation", t("COMMON_MASTERS_DESIGNATION_" + details.designation.i18key));
            } else {
                resetFormValues();
                alert("Searched Employee Code Does Not Exist!!");
            }
        } else {
            resetFormValues();
        }
    }, [test, setValue, isEmployeeLoading, t]);

    function resetFormValues() {
        setValue("enquiryCode", "");
        setValue("oldenquiryCode", "");
        setValue("orderNo", "");
        setValue("orderDate", "");
        setValue("enquirySubject", "");
        setValue("employee", "");
        setValue("department", "");
        setValue("designation", "");
    }

    const visibleColumns = [
        { Header: "Employee Code", accessor: "employeeCode" },
        { Header: "Name", accessor: "employeename" },
        { Header: "Department", accessor: "department" },
        { Header: "Designation", accessor: "designation" },
        { Header: "Mobile", accessor: "mobileNumber" },
        { Header: "Is Suspended?", accessor: "suspended" },
        { Header: "Suspention Order", accessor: "susOrderNumber" }
    ];
    function transformEmployeeData(employeeDataArray) {
        return employeeDataArray.map((employeeData) => {
          return {
            employeeCode: employeeData.employeeCode, // Keep the employee code
            employeename: employeeData.employeename, // Keep the employee name
            mobileNumber: employeeData.mobileNumber, // Keep the mobile number
            susOrderNumber: employeeData.susOrderNumber, // Keep the suspension order number
            department: employeeData?.department?.i18key 
              ? t(`COMMON_MASTERS_DEPARTMENT_${employeeData.department.i18key}`) 
              : t("COMMON_MASTERS_DEPARTMENT_UNKNOWN"), // Add prefix or default value for department
            designation: employeeData?.designation?.i18key 
              ? t(`COMMON_MASTERS_DESIGNATION_${employeeData.designation.i18key}`)
              : t("COMMON_MASTERS_DESIGNATION_UNKNOWN"), // Add prefix or default value for designation
            suspended: employeeData?.suspended?.value || "No", // Handle suspended value with default
          };
        });
      }
    if (isEmployeeLoading) {
        return <Loader />;
    }
    return (
        <React.Fragment>
            <form>
                <div className="bmc-card-row">
                    <div className="bmc-col3-card">

                        <CardLabel className="bmc-label">{t("EBE_ENQUIRY_CODE")}</CardLabel>
                        <Controller
                            control={control}
                            name="enquiryCode"
                            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                            render={(props) => (
                                <div>
                                    <TextInput
                                        disabled={true}
                                        readOnly={true}
                                        value={props.value}
                                        onChange={(e) => props.onChange(e.target.value)}
                                        onBlur={props.onBlur}
                                        t={t}
                                    />

                                </div>
                            )}
                        />

                    </div>
                    <div className="bmc-col3-card">

                        <CardLabel className="bmc-label">{t("EBE_ENQUIRY_CODE_OLD")}</CardLabel>
                        <Controller
                            control={control}
                            name="oldenquiryCode"
                            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                            render={(props) => (
                                <div>
                                    <TextInput
                                        disabled={true}
                                        readOnly={true}
                                        value={props.value}
                                        onChange={(e) => props.onChange(e.target.value)}
                                        onBlur={props.onBlur}
                                        t={t}
                                    />

                                </div>
                            )}
                        />

                    </div>
                    <div className="bmc-col3-card">

                        <CardLabel className="bmc-label">{t("EBE_ORDER_NO")}</CardLabel>
                        <Controller
                            control={control}
                            name="orderNo"
                            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                            render={(props) => (
                                <div>
                                    <TextInput
                                        disabled={true}
                                        readOnly={true}
                                        value={props.value}
                                        onChange={(e) => props.onChange(e.target.value)}
                                        onBlur={props.onBlur}
                                        t={t}
                                    />

                                </div>
                            )}
                        />

                    </div>
                    <div className="bmc-col3-card">

                        <CardLabel className="bmc-label">{t("EBE_ORDER_DATE")}</CardLabel>
                        <Controller
                            control={control}
                            name="orderDate"
                            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                            render={(props) => (
                                <div>
                                    <DatePicker disabled={true} date={props.value} onChange={props.onChange} onBlur={props.onBlur} />
                                </div>
                            )}
                        />

                    </div>

                </div>
                <div className="bmc-row-card">
                    <div className="bmc-col4-card">

                        <CardLabel className="bmc-label">{t("EBE_ENQUIRY_SUBJECT")}</CardLabel>
                        <Controller
                            control={control}
                            name="enquirySubject"
                            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                            render={(props) => (
                                <div>
                                    <TextArea
                                        disabled={true}
                                        readOnly={true}
                                        value={props.value}
                                        onChange={(e) => props.onChange(e.target.value)}
                                        onBlur={props.onBlur}
                                        name="enquirySubject"
                                        t={t}
                                    />
                                </div>
                            )}
                        />

                    </div>
                </div>
                <div className="bmc-row-card">
                    <div className="bmc-col3-card">

                        <CardLabel className="bmc-label">{t("EBE_EMPLOYEE")}</CardLabel>
                        <Controller
                            control={control}
                            name="employee"
                            rules={{
                                required: t("CORE_COMMON_REQUIRED_ERRMSG"),
                                maxLength: { value: 7, message: t("Employee code must be 7 characters long") },
                                // pattern: {
                                //   value: /^[A-Z]{4}0[A-Z0-9]{6}$/, // Regex pattern for IFSC code
                                //   message: t("Invalid EMPLOYEEcode") // Custom validation message
                                // }
                            }}
                            render={(props) => (
                                <div>
                                    <TextInput
                                        placeholder={t("EMPLOYEE CODE")}
                                        readOnly={true}
                                        disabled={true}
                                        value={props.value}
                                        onChange={props.onChange}
                                        onBlur={props.onBlur}
                                        t={t}
                                    />
                                </div>
                            )}
                        />

                    </div>
                    <div className="bmc-col3-card">

                        <CardLabel className="bmc-label">{t("EBE_EMP_NAME")}</CardLabel>
                        <Controller
                            control={control}
                            name="employeename"
                            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                            render={(props) => (
                                <div>
                                    <TextInput
                                        disabled={true}
                                        readOnly={true}
                                        value={props.value}
                                        onChange={(e) => props.onChange(e.target.value)}
                                        onBlur={props.onBlur}
                                        t={t}
                                    />
                                </div>
                            )}
                        />

                    </div>
                    <div className="bmc-col3-card">

                        <CardLabel className="bmc-label">{t("EBE_DEPARTMENT")}</CardLabel>
                        <Controller
                            control={control}
                            name="department"
                            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                            render={(props) => (
                                <div>
                                    <TextInput disabled={true} readOnly={true} value={props.value || ""} />
                                </div>
                            )}
                        />

                    </div>
                    <div className="bmc-col3-card">

                        <CardLabel className="bmc-label">{t("EBE_DESIGNATION")}</CardLabel>
                        <Controller
                            control={control}
                            name="designation"
                            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                            render={(props) => (
                                <div>
                                    <TextInput disabled={true} readOnly={true} value={t(props.value) || ""} />
                                </div>
                            )}
                        />

                    </div>

                </div>
                <div className="bmc-card-row">
                    <div className="bmc-row-card-header">
                        <CustomTable
                            t={t}
                            columns={visibleColumns}
                            data={data}
                            manualPagination={false}
                            totalRecords={totalRecords}
                            sortParams={sortParams}
                        //  fileName="YourCustomFileName"
                        />
                    </div>
                </div>
            </form>
        </React.Fragment>
    );
};

export default PreDetail;