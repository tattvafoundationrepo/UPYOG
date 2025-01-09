import { AddIcon, Dropdown, RemoveIcon, TextInput, Toast } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import dropdownOptions from "../pagecomponents/dropdownOptions.json";
import { useMemo } from "react";
import CustomTable from './CustomTable';
import CustomModal from './CustomModal'
import { CardLabel, LabelFieldPair } from "@upyog/digit-ui-react-components";
import TableCard from "@tattvafoundation/digit-ui-module-deonar/src/pages/employee/commonFormFields/tableCard";

const QualificationCard = ({ tenantId, onUpdate, initialRows = [], AddOption = true, AllowRemove = true, AllowEdit = true }) => {
  const { t } = useTranslation();

  const initialDefaultValues = useMemo(() => {
    return {
      qualification: null,
      yearOfPassing: null,
      percentage: null,
      board: null,
    };
  }, []);
  const userDetails = Digit.UserService.getUser();
  const [userDetail, setUserDetail] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isDisabled, setIsDisabled] = useState(false)
  const [documents, setDocuments] = useState([])
  const [qualifications, setQualifications] = useState([]);
  const [isEditing, setIsEditing] = useState(false); 
  const [selectedRow, setSelectedRow] = useState(null);
  const [isMobileView, setIsMobileView] = useState(window.innerWidth < 769);
  const [toast, setToast] = useState('')

  const processQualificationData = (items, headerLocale) => {
    if (items.length === 0) return [];
    return items
      .map((item) => {
        if (typeof item === "object" && item.qualificationId && item.qualification) {
          return {
            qualification: {
              id: item.qualificationId,
              qualification: item.qualification,
              i18nKey: `${headerLocale}_ADMIN_${item.qualification}`,
            },
            percentage: item.percentage,
            yearOfPassing: { label: item.yearOfPassing, value: item.yearOfPassing },
            board: { label: item.board, value: item.board },
          };
        }
        return null;
      })
      .filter((item) => item !== null);
  };

  const processCommonData = (data, headerLocale) => {
    return (
      data?.CommonDetails?.map((item) => ({
        id: item.id,
        qualification: item.name,
        i18nKey: `${headerLocale}_ADMIN_${item.name}`,
      })) || []
    );
  };

  const headerLocale = Digit.Utils.locale.getTransformedLocale(tenantId);

  const qualificationFunction = (data) => {
    const qualificationData = processCommonData(data, headerLocale);
    setQualifications(qualificationData);
    return { qualificationData };
  };

  const getQualification = { CommonSearchCriteria: { Option: "qualification" } };
  Digit.Hooks.bmc.useCommonGet(getQualification, { select: qualificationFunction });

  const {
    control,
    handleSubmit,
    reset,
    getValues,
    formState: { errors },
  } = useForm({
    defaultValues: initialDefaultValues,
  });

  const [rows, setRows] = useState([]);
  useEffect(() => {
    const processedRows = processQualificationData(initialRows, headerLocale);
    if (!processedRows || processedRows.length === 0) {
      //setRows([initialDefaultValues]); // Ensure at least one row is available
    } else {
      setRows(processedRows);
    }
  }, [initialRows, headerLocale, initialDefaultValues]);
  const dynamicStartYear = new Date().getFullYear() - 49;
  const currentYear = new Date().getFullYear();

  const years = Array.from({ length: currentYear - dynamicStartYear + 1 }, (v, k) => ({
    label: `${dynamicStartYear + k}`,
    value: dynamicStartYear + k,
  }));
  const saveQualifications = Digit.Hooks.bmc.useSaveQualification()
  const addRow = () => {
    const formData = getValues(); // Get form data
    const isDuplicate = rows.some((row) => row.qualification.qualification === formData.qualification.qualification);
  
    if (isDuplicate) {
      alert(t("Qualification already exists. Please select a different qualification."));
      return; // Stop further processing if duplicate is found
    }

    // Construct the new row with the required structure
    const newRow = {
      updatedQualifications: [
        {
          qualification: formData.qualification || "",
          yearOfPassing: formData.yearOfPassing || "",
          board: formData.board || "",
          percentage: formData.percentage || "",
        },

      ]

    };

    // Save the document data (API call)
    saveQualifications.mutate(newRow, {
      onSuccess: (response) => {

        // Update the rows state with the new data
        setRows((prevRows) => [
          ...prevRows,
          ...newRow.updatedQualifications.map((doc) => ({
            qualification: doc.qualification,
            yearOfPassing: doc.yearOfPassing,
            board: doc.board,
            percentage: doc.percentage,
          })),
        ]);
        setToastWithTimeout("success", t('QUALIFICATION ADDED SUCCESSFULLY'))

        // Close the modal
        toggleModal();

        // Reset form to default values
        reset(initialDefaultValues);

        // Call onUpdate callback if provided
        if (onUpdate) {
          onUpdate([
            ...rows,
            ...newRow.updatedQualifications.map((doc) => ({
              qualification: doc.qualification,
              yearOfPassing: doc.yearOfPassing,
              board: doc.board,
              percentage: doc.percentage,
            })),
          ]);
        }
      },
      onError: (error) => {
        console.error("Failed to save document:", error);
      },
    });
  };
  const setToastWithTimeout = (key, action) => {
    setToast({ key, action });
  
    setTimeout(() => {
      setToast(null);
    }, 3000);
  };

  const removeRows = Digit.Hooks.bmc.useRemoveDocuments()

  const removeQualificationRow = (id) => {
    // Find the document by id
    const QualificationToRemove = rows.find((item) => item.qualification.id === id);

    if (!QualificationToRemove) {
      console.error("No matching qualification found with the given id:", id);
      return;
    }

    const payload = {
      removalcriteria: {
        id: QualificationToRemove.qualification.id,
        option: 'qualification',
      },
    };

    // Call remove API
    removeRows.mutate(payload, {
      onSuccess: () => {
        // Remove the document from rows state
        setRows((prevRows) => prevRows.filter((row) => row.qualification.id !== id));
        setToastWithTimeout("success", t('QUALIFICATION REMOVED SUCCESSFULLY'))

      },
      
      onError: (error) => {
        console.error("Failed to delete document row:", error);
      },
    });
  };



  const userFunction = (data) => {
    if (data && data.UserDetails && data.UserDetails.length > 0) {
      setUserDetail(data.UserDetails[0]);
    }
  };

  const getUserDetails = { UserSearchCriteria: { Option: "full", TenantID: tenantId, UserID: userDetails?.info?.id } };
  Digit.Hooks.bmc.useUsersDetails(getUserDetails, { select: userFunction });


  const visibleColumns = (handleOnClick) => [
    {
      Header: t("QUALIFICATION"),
      accessor: t("qualification.i18nKey"),
      sortable: true,
      Cell: ({ row }) => (
        <span
          onClick={AllowEdit ? () => handleOnClick(row.original.qualification.qualification) : null}
          style={{ cursor: AllowEdit ? "pointer" : "default", color: AllowEdit ? "red" : "black" }}
        >
          {t(row.original.qualification.i18nKey)}
        </span>
      ),
      isVisible: true,
    },
    { Header: t("BMC_YEAR_OF_PASSING"), accessor: "yearOfPassing.value" },
    { Header: t("BMC_BOARD"), accessor: "board.value" },
    { Header: t("BMC_PERCENTAGE"), accessor: "percentage",Cell: ({ value }) => t(value)+'%'},
    {
      Header: t("Actions"),
      accessor: "action",
      Cell: ({ row }) => (
        <span
          onClick={AllowRemove ? () => removeQualificationRow(row.original.qualification.id) : null}
          style={{
            cursor: AllowRemove ? "pointer" : "not-allowed",
            color: AllowRemove ? "blue" : "gray",
          }}
        >
          <RemoveIcon />
        </span>
      ),
    },
  ];

  const fields = [
    {
      key: "qualification.i18nKey",
      label: t("QUALIFICATION"),
      display: (data) => t(data.qualification.i18nKey) || "N/A", // Safely access nested value
    },
    {
      key: "yearOfPassing.value",
      label: t("BMC_YEAR_OF_PASSING"),
    },
    { label: t("BMC_BOARD"), key: "board.value" },
    {
      key: "percentage",
      label: t("BMC_PERCENTAGE"),
    },
    {
      key: "action",
      label: t("Actions"),
      display: (data) => (
        <button
          onClick={() => removeQualificationRow(data?.qualification?.id)}
          style={{
            cursor: "pointer",
            color: "white",
            backgroundColor: "red",
            border: "none",
            borderRadius: "4px",
            padding: "4px 8px",
            fontSize: "14px",
          }}
        >
          {t("Delete")}
        </button>
      ),
    },
  ];
  const handleAddEmployee = () => {
    setIsModalOpen(!isModalOpen);
  };
  const myConfig = {
    elements: [
      {
        label: { heading: t("BMC_ADD_QUALIFICATION"), cancel: "Cancel", submit: "Submit" },
        type: "p",
        text: t("BMC_ADD_QUALIFICATION"),
        style: { textDecoration: "underline", cursor: "pointer" },
        onClick: "onAddDocumentClick",
      },
      {
        label: { heading: "Add Document Number", cancel: "Cancel", submit: "Submit" },
      },
    ],
  };
  const toggleModal = () => setIsModalOpen(!isModalOpen);


  const documentFunction = (data) => {
    const documentsData = processCommonData(data, headerLocale);
    setDocuments(documentsData);
    return { documentsData };
  };
  const getDocuments = { CommonSearchCriteria: { Option: "qualification" } };
  Digit.Hooks.bmc.useCommonGet(getDocuments, { select: documentFunction });
  const handleOnClick = (rowDocument) => {
    const rowToEdit = rows.find((row) => row.qualification.qualification === rowDocument);
    if (rowToEdit) {
      setSelectedRow(rowToEdit);
      setIsEditing(true);
      reset({
        qualification: rowToEdit.qualification, // Populate modal fields
        yearOfPassing: rowToEdit.yearOfPassing,
        board: rowToEdit.board,
        percentage: rowToEdit.percentage,
      });
      toggleModal(); // Open modal
    }
  };
  const updateRow = () => {
    const formData = getValues(); // Get updated form data
    setRows((prevRows) =>
      prevRows.map((row) =>
        row.qualification.value === selectedRow.qualification.value
          ? {
            ...row,
            qualification: formData.qualification || "",
            yearOfPassing: formData.yearOfPassing || "",
            board: formData.board || "",
            percentage: formData.percentage || "",
          }
          : row
      )
    );
    if (onUpdate) {
      onUpdate(rows);
    }
    toggleModal(); // Close modal
    setIsEditing(false); // Reset edit mode
    reset(initialDefaultValues); // Reset form
  };

  const handleUUIDClick = (entryUnitId) => {
    setIsModalOpen(!isModalOpen);
  };

  useEffect(() => {
    const handleResize = () => {
      setIsMobileView(window.innerWidth < 769);
    };
    window.addEventListener("resize", handleResize);
    handleResize();
    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  return (
    <React.Fragment>
      <div className="bmc-row-card-header">

        <div className="bmc-title">{t("BMC_QUALIFICATION_DETAILS")}</div>
        {isMobileView && rows.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)}
        {!isMobileView && (
        <CustomTable
          t={t}
          columns={visibleColumns(handleOnClick)}
          data={rows}
          manualPagination={false}
          onAddEmployeeClick={handleAddEmployee}
          config={myConfig}
          tableClassName={"ebe-custom-scroll"}
          showSearch={AllowEdit ? true : false}
          showText={AllowEdit ? true : false}
        />
        )}
        <CustomModal
          isOpen={isModalOpen}
          onClose={() => {
            toggleModal();
            setIsEditing(false); // Reset edit mode on close
            reset(initialDefaultValues); // Reset form values
          }}
          title={<h1 style={{marginLeft:'0px'}} className="heading-m">{isEditing ? t("Edit Qualification") : t("Add Qualification")}</h1>}
          actionCancelLabel={t("Cancel")}
          actionCancelOnSubmit={() => {
            toggleModal();
            setIsEditing(false); // Reset edit mode on cancel
            reset(initialDefaultValues); // Reset form values
          }}
          actionSaveLabel={t(isEditing ? t("Update") : t("Submit"))}
          actionSaveOnSubmit={handleSubmit(isEditing ? updateRow : addRow)}
          formId="modal-action"
        >
          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }} className="bmc-col11-card">


            <div style={{ width: '300px' }}>
              <LabelFieldPair>
                <CardLabel className="bmc-label">
                  {t("QUALIFICATION")}
                  {errors.qualification && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.qualification.message}</sup>}
                </CardLabel>
                <Controller
                  control={control}
                  name="qualification"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <Dropdown
                        placeholder={t("SELECT THE EDUCATION QUALIFICATION")}
                        selected={props.value}
                        select={(qualification) => props.onChange(qualification)}
                        option={qualifications}
                        optionKey="i18nKey"
                        t={t}
                        className="employee-select-wrap bmc-form-field"
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
            <div style={{ width: "300px" }}>
              <LabelFieldPair>
                <CardLabel className="bmc-label">
                  {t("BMC_YEAR_OF_PASSING")}
                  {errors.yearOfPassing && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.yearOfPassing.message}</sup>}
                </CardLabel>
                <Controller
                  control={control}
                  name="yearOfPassing"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <Dropdown
                        placeholder={t("BMC_YEAR_OF_PASSING")}
                        selected={props.value}
                        select={(year) => props.onChange(year)}
                        option={years}
                        optionKey="value"
                        t={t}
                        className="employee-select-wrap bmc-form-field"
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
            <div style={{ width: "300px" }}>
              <LabelFieldPair>
                <CardLabel className="bmc-label">
                  {t("BMC_BOARD")}
                  {errors.board && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.board.message}</sup>}
                </CardLabel>
                <Controller
                  control={control}
                  name="board"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <Dropdown
                        placeholder={t("SELECT BOARD")}
                        selected={props.value}
                        select={(board) => props.onChange(board)}
                        option={dropdownOptions.board}
                        optionKey="value"
                        t={t}
                        className="employee-select-wrap bmc-form-field"
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
            <div style={{ width: "300px" }}>
              <LabelFieldPair>
                <CardLabel className="bmc-label">
                  {t("BMC_PERCENTAGE")}
                  {errors.percentage && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.percentage.message}</sup>}
                </CardLabel>
                <Controller
                  control={control}
                  name="percentage"
                  rules={{
                    required: t("CORE_COMMON_REQUIRED_ERRMSG"),
                    min: {
                      value: 0,
                      message: t("PERCENTAGE MUST BE AT LEAST 0"),
                    },
                    max: {
                      value: 100,
                      message: t("PERCENTAGE MUST BE AT MOST 100"),
                    },
                  }}
                  render={(props) => (
                    <div>
                      <TextInput
                        placeholder={t("BMC_PERCENTAGE")}
                        value={props.value}
                        onChange={props.onChange}
                        type={"number"}
                        onBlur={props.onBlur}
                        t={t}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
        </CustomModal>
      </div>
      {toast && (
        <Toast
          error={toast.key === "error"}
          label={t(toast.action)}
          onClose={() => setToast(null)}
          style={{ maxWidth: "670px" }}
        />
      )}
    </React.Fragment>
  );
};

export default QualificationCard;
