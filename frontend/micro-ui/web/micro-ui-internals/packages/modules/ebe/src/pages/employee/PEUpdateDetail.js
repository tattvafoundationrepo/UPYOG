import {
  CardLabel,
  DatePicker,
  Dropdown,
  LabelFieldPair,
  TextInput,
  TextArea,
  Toast,
  FormComposer,
  EditIcon,
  RemoveIcon,
  Modal,
} from "@upyog/digit-ui-react-components";
import isEqual from "lodash.isequal";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import CustomTable from "../../components/customTable";
import CustomModal from "../../components/customModal";
import { PEConfig } from "../../components/config/PEConfig";
import { useParams } from "react-router-dom";
import TableCard from "@tattvafoundation/digit-ui-module-deonar/src/pages/employee/commonFormFields/tableCard";

const PEUpdateDetail = (defaultValues) => {
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const appmutate = Digit.Hooks.ebe.useEBECreateEnquiry();
  const [data, setData] = useState([]);
  const [totalRecords, setTotalRecords] = useState(0);
  const [sortParams, setSortParams] = useState({});
  let [department, setDepartment] = useState([]);
  let [designation, setDesignation] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalSubmit, setModalSubmit] = useState(true);
  const [isEnableLoader, setIsEnableLoader] = useState(false);
  const [showToast, setShowToast] = useState(null);
  // const [formData, setFormData] = useState({
  //     employeeAdd: {}  // Assuming 'employeeAdd' is `config.key`
  // });
  const [isMobileView, setIsMobileView] = useState(window.innerWidth < 768);

  let myData = {};

  const myconfig = {
    elements: [
      {
        label: { heading: "Add Employee", cancel: "Cancel", submit: "Submit" },
        type: "p",
        text: "Add Employee",
        style: { textDecoration: "underline", cursor: "pointer" },
        onClick: "onAddEmployeeClick",
      },
    ],
  };
  const { control, setValue, handleSubmit, getValues } = useForm();

  const { data: MDMSData, isLoading } = Digit.Hooks.hrms.useHrmsMDMS(tenantId, "egov-hrms", "HRMSRolesandDesignation");

  function getdepartmentdata() {
    return department.map((ele) => {
      ele["i18key"] = t("COMMON_MASTERS_DEPARTMENT_" + ele.code);
      return ele;
    });
  }

  function getdesignationdata() {
    return designation.map((ele) => {
      ele["i18key"] = t("COMMON_MASTERS_DESIGNATION_" + ele.code);
      return ele;
    });
  }

  useEffect(() => {
    if (!isLoading && MDMSData) {
      const departmentData = MDMSData?.MdmsRes?.["common-masters"]?.Department || [];
      const designationData = MDMSData?.MdmsRes?.["common-masters"]?.Designation || [];
      setDepartment(departmentData);
      setDesignation(designationData);
    }
  }, [data, isLoading, setValue]);

  const initialData = useParams();

  const { data: test, isLoading: isEmployeeLoading } = Digit.Hooks.ebe.useGetEnquiry({ enqId: initialData.id, type: initialData.type }, true);

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

  useEffect(() => {
    if (!isEmployeeLoading && test) {
      const details = test?.peEnquiryList[0].data1 || {};
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
  const handleUUIDClick = (entryUnitId) => {
    //setSelectedUUID(entryUnitId);
    //sessionStorage.setItem("WorkingID",entryUnitId);
    setIsModalOpen(!isModalOpen);
  };

  const visibleColumns = [
    { Header: "Employee Code", accessor: "employeeCode" },
    { Header: "Name", accessor: "employeename" },
    { Header: "Department", accessor: "department" },
    { Header: "Designation", accessor: "designation" },
    { Header: "Mobile", accessor: "mobileNumber" },
    { Header: "Is Suspended?", accessor: "suspended" },
    { Header: "Suspention Order", accessor: "susOrderNumber" },
    {
      Header: "Actions",
      accessor: "action",
      Cell: ({ row }) => (
        <span onClick={() => handleUUIDClick(row.original.employeeCode)} style={{ cursor: "pointer", color: "blue" }}>
          <RemoveIcon />
        </span>
      ),
    },
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

  const handleAddEmployee = () => {
    console.log("Add Employee button clicked");
    setIsModalOpen(!isModalOpen);
    // Implement your logic here (e.g., open a modal, navigate to a new page, etc.)
  };

  const toggleModal = () => {
    if (isModalOpen) {
      // Clear the session storage when the modal closes
      // sessionStorage.removeItem("WorkingID");
    }
    setIsModalOpen(!isModalOpen);
    setShowToast({ warning: true, label: `WF_ACTION_CANCELLED` });
    closeToast();
  };

  const closeToast = useCallback(() => {
    setTimeout(() => setShowToast(null), 5000);
  }, []);

  const _submit = () => {
    setIsModalOpen(false);
    setIsEnableLoader(true);
    const enqId = getValues("enquiryCode");
    const appmutateObj = { ...myData, enqId };
    appmutate.mutate(appmutateObj, {
      onSuccess: (data, variables) => {
        //window.location.reload();
        setShowToast({
          label: Digit.Utils.locale.getTransformedLocale(`SUCCESS_EMPLOYEE_ADD`),
        });
        //const workflowmutateObj = { ...mydata, selectedAction };
        // Perform the second mutation only after the first one is successful
      },
      onError: (error, variables) => {
        setIsEnableLoader(false);
        //window.location.reload();
        setShowToast({
          error: true,
          label: Digit.Utils.locale.getTransformedLocale(`ERROR_EMPLOYEE_ADD`),
          isDleteBtn: true,
        });
        // callback?.onError?.();
      },
    });
  };

  const onFormValueChange = (setValue = true, updatedData) => {
    myData = updatedData;
  };

  const fields = [
    { key: "employeename", label: "Employee Name" },
    { key: "department", label: "Department" },
    { key: "designation", label: "Designation" },
    { key: "mobileNumber", label: "Mobile Number" },
    { key: "suspended", label: "Suspended" },
    // {
    //     Header: "Actions", accessor: "action", Cell: ({ row }) => (
    //         <span onClick={() => handleUUIDClick(row.original.employeeCode)} style={{ cursor: "pointer", color: "blue" }}>
    //            <RemoveIcon />
    //         </span>

    //     )
    // },
    { key: "employeeCode", label: "Employee Code", isClickable: true },
  ];

  useEffect(() => {
    const handleResize = () => {
      setIsMobileView(window.innerWidth < 768);
    };
    window.addEventListener("resize", handleResize);
    handleResize();
    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  return (
    <React.Fragment>
      <div>
        <div className="bmc-card-row">
          <div className="bmc-col-large-header">
            <div className="bmc-title">{initialData.type === '0' ? t("EBE_PE_TITLE") : t("EBE_DE_TITLE")}</div>
          </div>
        </div>
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
                  <TextInput value={props.value} readOnly={true} onChange={(e) => props.onChange(e.target.value)} onBlur={props.onBlur} t={t} />
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
                  <TextInput value={props.value} readOnly={true} onChange={(e) => props.onChange(e.target.value)} onBlur={props.onBlur} t={t} />
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
                  <DatePicker date={props.value} onChange={props.onChange} onBlur={props.onBlur} disabled={true} />
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
                    value={props.value}
                    readOnly={true}
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
                  <Dropdown
                    placeholder={t("SELECT DEPARTMENT")}
                    readOnly={true}
                    selected={props.value}
                    select={props.onChange}
                    onBlur={props.onBlur}
                    option={getdepartmentdata() || []}
                    optionKey={"i18key"}
                    t={t}
                    isMandatory={true}
                    className="employee-select-wrap bmc-form-field"
                  />
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
                  <Dropdown
                    placeholder={t("SELECT DESIGNATION")}
                    readOnly={true}
                    selected={props.value}
                    select={props.onChange}
                    onBlur={props.onBlur}
                    option={getdesignationdata() || []}
                    optionKey={"i18key"}
                    t={t}
                    isMandatory={true}
                    className="employee-select-wrap bmc-form-field"
                  />
                </div>
              )}
            />
          </div>
        </div>
        <div className="bmc-card-row">
          <div className="bmc-row-card-header">
            {isMobileView && data.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)}
            {!isMobileView && (
              <CustomTable
                t={t}
                columns={visibleColumns}
                data={data}
                manualPagination={false}
                totalRecords={totalRecords}
                sortParams={sortParams}
                onAddEmployeeClick={handleAddEmployee}
                config={myconfig}
                tableClassName={"ebe-custom-scroll"}
                //  fileName="YourCustomFileName"
              />
            )}
          </div>
        </div>
        <CustomModal
          isOpen={isModalOpen}
          onClose={toggleModal}
          title={<h1 className="heading-m">{t(myconfig.elements[0].label.heading)}</h1>}
          actionCancelLabel={t(myconfig.elements[0].label.cancel)}
          actionCancelOnSubmit={toggleModal}
          actionSaveLabel={t(myconfig.elements[0].label.submit)}
          actionSaveOnSubmit={handleSubmit(_submit)}
          formId="modal-action"
          isDisabled={!modalSubmit}
        >
          <FormComposer
            formId={"modal-action"}
            defaultValues={defaultValues}
            config={PEConfig("UPDATE")}
            onSubmit={handleSubmit(_submit)}
            onFormValueChange={onFormValueChange}
            isDisabled={true}
            label={t("ADD_EMPLOYEE")}
          />
        </CustomModal>
      </div>
      {showToast && (
        <Toast
          error={showToast?.error}
          warning={t(showToast?.warning)}
          label={t(showToast?.label)}
          onClose={() => setShowToast(null)}
          isDleteBtn={showToast?.isDleteBtn}
        />
      )}
    </React.Fragment>
  );
};

export default PEUpdateDetail;
