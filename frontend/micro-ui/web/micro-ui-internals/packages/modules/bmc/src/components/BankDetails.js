import { AddIcon, CardLabel, Header, LabelFieldPair, RemoveIcon, TextInput, Toast } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState, useMemo } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import CustomTable from './CustomTable';
import CustomModal from './CustomModal'
import TableCard from "@tattvafoundation/digit-ui-module-deonar/src/pages/employee/commonFormFields/tableCard";

const BankDetailsForm = ({ tenantId, onUpdate, initialRows = [], AddOption = true, AllowRemove = true, AllowEdit = true }) => {
  const { t } = useTranslation();
  const initialDefaultValues = useMemo(() => {
    return {
      branchId: "",
      name: "",
      branchName: "",
      ifsc: "",
      micr: "",
      accountNumber: "",
    };
  }, []);

  const {
    control,
    handleSubmit,
    reset,
    getValues,
    setValue,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: initialDefaultValues,
  });

  const headerLocale = Digit.Utils.locale.getTransformedLocale(tenantId);
  const [bankData, setBankData] = useState([]);
  const [rows, setRows] = useState([]);
  const ifsc = watch("ifsc");
  const [isEditing, setIsEditing] = useState(false);
  const [selectedRow, setSelectedRow] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isMobileView, setIsMobileView] = useState(window.innerWidth < 769);
  const [toast, setToast] = useState('')

  const userDetails = Digit.UserService.getUser();


  const processBankData = (data, headerLocale, array) => {
    if (array) {
      if (data.length === 0) return [];
      return (
        data?.BankDetails?.filter((item) => item.branchId !== 0).map((item) => ({
          branchId: item.branchId,
          name: item.name,
          branchName: item.branchName,
          ifsc: item.ifsc,
          micr: item.micr,
          accountNumber: item.accountNumber,
          i18nKey: `${headerLocale}_ADMIN_${item.name}`,
        })) || []
      );
    } else {
      if (!data) return [];
      return (
        data
          .filter((item) => item.branchId !== 0)
          .map((item) => ({
            branchId: item.branchId,
            name: item.name,
            branchName: item.branchName,
            ifsc: item.ifsc,
            micr: item.micr,
            accountNumber: item.accountNumber,
            i18nKey: `${headerLocale}_ADMIN_${item.name}`,
          })) || []
      );
    }
  };

  const bankFunction = (data) => {
    const BankData = processBankData(data, headerLocale, true);
    setBankData(BankData);
    return { BankData };
  };

  const getBank = { BankSearchCriteria: { IFSC: ifsc } };
  Digit.Hooks.bmc.useCommonGetBank(getBank, { select: bankFunction });

  useEffect(() => {
    if (ifsc && ifsc.length === 11) {
      const details = bankData.find((bank) => bank.ifsc === ifsc) || {};
      setValue("branchId", details.branchId || "");
      setValue("name", details.name || "");
      setValue("branchName", details.branchName || "");
      setValue("micr", details.micr || "");
    } else {
      setValue("branchId", "");
      setValue("name", "");
      setValue("branchName", "");
      setValue("micr", "");
    }
  }, [ifsc, bankData, setValue]);

  useEffect(() => {
    const processedRows = processBankData(initialRows, headerLocale, false);
    setRows(processedRows);
  }, [initialRows, headerLocale]);

  const saveBankDetails = Digit.Hooks.bmc.useSaveBankDetails();
  const setToastWithTimeout = (key, action) => {
    setToast({ key, action });
  
    setTimeout(() => {
      setToast(null);
    }, 3000);
  };
  const addRow = () => {
    const formData = getValues();
    const isDuplicate = rows.some((row) => row.accountNumber === formData.accountNumber);
  
    if (isDuplicate) {
      alert(t("Bank account already exists. Please select a different one."));
      return;
    }
    const branchId = bankData.find((item) => {
      return item.branchId;
    });
    const newRow = {
      updatedBank: [
        {
          branchId: branchId.branchId,
          name: formData.name,
          branchName: formData.branchName,
          ifsc: formData.ifsc,
          micr: formData.micr,
          accountNumber: formData.accountNumber,
        },
      ],
    };
    saveBankDetails.mutate(newRow, {
      onSuccess: (data) => {

        setRows((prevRows) => [
          ...prevRows,
          ...newRow.updatedBank.map((doc) => ({
            name: doc.name,
            branchName: doc.branchName,
            ifsc: doc.ifsc,
            micr: doc.micr,
            accountNumber: doc.accountNumber,

          })),
        ]);
        setToastWithTimeout('success', t('BANK DETAIL ADDED SUCCESSFULLY'))


        toggleModal();

        reset(initialDefaultValues);

        if (onUpdate) {
          onUpdate([
            ...rows,
            ...newRow.updatedBank.map((doc) => ({
              name: doc.name,
              branchName: doc.branchName,
              ifsc: doc.ifsc,
              micr: doc.micr,
              accountNumber: doc.accountNumber,
            })),
          ]);
        }
      },
      onError: (error) => {
        console.error("Failed to save bank details:", error);
      },
    });

  };

  const removeRows = Digit.Hooks.bmc.useRemoveDocuments();

  const removeBankRow = (id) => {
    // Find the document by id
    const bankToRemove = rows.find((item) => item.accountNumber === id);

    if (!bankToRemove) {
      console.error("Document not found");
      return;
    }

    const payload = {
      removalcriteria: {
        id: bankToRemove.accountNumber,
        option: "bank",
      },
    };
    removeRows.mutate(payload, {
      onSuccess: () => {
        setRows((prevRows) => prevRows.filter((row) => row.accountNumber !== id));
        setToastWithTimeout('success', t('BANK DETAIL REMOVED SUCCESSFULLY'))

      },
      onError: (error) => {
        console.error("Failed to delete document row:", error);
      },
    });
  };

  const visibleColumns = () => [
    {
      Header: t("BMC_IFSC Code"),
      accessor: "ifsc",

      sortable: true,
      Cell: ({ row }) => (
        <span
          onClick={AllowEdit ? () => handleOnClick(row.original.ifsc) : null}
          style={{ cursor: AllowEdit ? "pointer" : "default", color: AllowEdit ? "red" : "black" }}
        >
          {row.original.ifsc}
        </span>
      ),
      isVisible: true,
    },
    { Header: t("BMC_MICR Code"), accessor: "micr" },
    { Header: t("BMC_Account Number"), accessor: "accountNumber" },
    { Header: t("BMC_BANK NAME"), accessor: "name" },
    { Header: t("BMC_BRANCH NAME"), accessor: "branchName" },

    {
      Header: t("Actions"),
      accessor: "action",
      Cell: ({ row }) => (
        <span
          onClick={AllowRemove ? () => removeBankRow(row.original.accountNumber) : null}
          style={{ cursor: AllowRemove ? "pointer" : "not-allowed", color: AllowRemove ? "blue" : "gray" }}
        >
          <RemoveIcon />
        </span>
      ),
    },
  ];

  const fields = [
    {
      key: "ifsc",
      label: t("BMC_ADD_DOCUMENT"),
      display: (data) => data?.ifsc || "N/A", // Safely access nested value
    },
    {
      key: "micr",
      label: t("BMC_MICR Code"),
    },
    {
      key: "accountNumber",
      label: t("BMC_Account Number"),
    },
    {
      key: "name",
      label: t("BMC_BANK NAME"),
    },
    {
      key: "branchName",
      label: t("BMC_BRANCH NAME"),
    },
    {
      key: "action",
      label: t("Actions"),
      display: (data) => (
        <button
          onClick={() => removeBankRow(data?.accountNumber)}
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
        label: { heading: t("BMC_ADD_BANK_DETAIL"), cancel: "Cancel", submit: "Submit" },
        type: "p",
        text: t("BMC_ADD_BANK_DETAIL"),
        style: { textDecoration: "underline", cursor: "pointer" },
        onClick: "onAddDocumentClick",
      },
      {
        label: { heading: "Add Document Number", cancel: "Cancel", submit: "Submit" },
      },
    ],
  };
  const toggleModal = () => setIsModalOpen(!isModalOpen);

  const handleOnClick = (rowDocument) => {
    if (!AllowEdit) return;
    const rowToEdit = rows.find((row) => row.ifsc === rowDocument);
    if (rowToEdit) {
      setSelectedRow(rowToEdit);
      setIsEditing(true);
      reset({
        name: rowToEdit.name,
        branchName: rowToEdit.branchName,
        ifsc: rowToEdit.ifsc,
        micr: rowToEdit.micr,
        accountNumber: rowToEdit.accountNumber,
      });
      toggleModal();
    }
  };


  const updateRow = () => {
    const formData = getValues();
    setRows((prevRows) =>
      prevRows.map((row) =>
        row.name === selectedRow.name
          ? {
            ...row,
            name: formData.name,
            branchName: formData.branchName,
            ifsc: formData.ifsc,
            micr: formData.micr,
            accountNumber: formData.accountNumber,
          }
          : row
      )
    );
    if (onUpdate) {
      onUpdate(rows);
    }
    toggleModal();
    setIsEditing(false);
    reset(initialDefaultValues);
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

  const handleUUIDClick = (entryUnitId) => {
    setIsModalOpen(!isModalOpen);
  };

  return (
    <React.Fragment>
      <div className="bmc-row-card-header">
        <div className="bmc-card-row">
          <div className="bmc-title">{t("BMC_BANK DETAILS")}</div>
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
              setIsEditing(false);
              reset(initialDefaultValues);
            }}
            title={<h1 style={{marginLeft:'0px'}} className="heading-m">{isEditing ? t("Edit Bank") : t("Add Banks")}</h1>}
            actionCancelLabel={t("Cancel")}
            actionCancelOnSubmit={() => {
              toggleModal();
              setIsEditing(false);
              reset(initialDefaultValues);
            }}
            actionSaveLabel={(isEditing && AllowEdit ? t("Update") : t("Submit"))}
            actionSaveOnSubmit={handleSubmit(isEditing && AllowEdit ? updateRow : addRow)}
            formId="modal-action"
          >
            <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }} className="bmc-col11-card">

              <div style={{ width: '300px' }}>
                <LabelFieldPair>
                  <CardLabel className="bmc-label">
                    {t("BMC_IFSC Code")}
                    {errors.ifsc && <sup style={{ color: "red", fontSize: "x-small", paddingLeft: "10px" }}>{errors.ifsc.message}</sup>}
                  </CardLabel>
                  <Controller
                    control={control}
                    name="ifsc"
                    rules={{
                      required: t("CORE_COMMON_REQUIRED_ERRMSG"),
                      maxLength: { value: 11, message: t("IFSC code must be 11 characters long") },
                      pattern: {
                        value: /^[A-Z]{4}0[A-Z0-9]{6}$/,
                        message: t("Invalid IFSC code"),
                      },
                    }}
                    render={(props) => (
                      <div>
                        <TextInput placeholder={t("IFSC CODE")} value={props.value} onChange={props.onChange} onBlur={props.onBlur} t={t} />
                      </div>
                    )}
                  />
                </LabelFieldPair>
              </div>
              <div style={{ width: "300px" }}>
                <LabelFieldPair>
                  <CardLabel className="bmc-label">
                    {t("BMC_MICR Code")}
                    {errors.micr && <sup style={{ color: "red", fontSize: "x-small", paddingLeft: "10px" }}>{errors.micr.message}</sup>}
                  </CardLabel>
                  <Controller
                    control={control}
                    name="micr"
                    render={(props) => (
                      <div>
                        <TextInput {...props} placeholder={t("MICR CODE")} disabled />
                        {errors.micr && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.micr.message}</sup>}
                      </div>
                    )}
                  />
                </LabelFieldPair>
              </div>
              <div style={{ width: "300px" }}>
                <LabelFieldPair>
                  <CardLabel className="bmc-label">
                    {t("BMC_Account Number")}
                    {errors.title && <sup style={{ color: "red", fontSize: "x-small", paddingLeft: "10px" }}>{errors.title.message}</sup>}
                  </CardLabel>
                  <Controller
                    control={control}
                    name="accountNumber"
                    rules={{
                      required: t("CORE_COMMON_REQUIRED_ERRMSG"),
                      minLength: { value: 8, message: t("Account Number must be 8 - 18 characters long") },
                      maxLength: { value: 18, message: t("Account Number must be 8 - 18 characters long") },
                      pattern: {
                        value: /^(?=\S{8,18}$)[A-Za-z0-9]+$/,
                        message: t("Invalid Account Number"),
                      },
                    }}
                    render={(props) => (
                      <div>
                        <TextInput {...props} placeholder={t("ACCOUNT NUMBER")} type="Text" value={props.value} onChange={props.onChange} />
                        {errors.accountNumber && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.accountNumber.message}</sup>}
                      </div>
                    )}
                  />
                </LabelFieldPair>
              </div>
              <div style={{ width: "300px" }}>
                <LabelFieldPair>
                  <CardLabel className="bmc-label">
                    {t("BMC_BANK NAME")}
                    {errors.title && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.title.message}</sup>}
                  </CardLabel>
                  <Controller
                    control={control}
                    name="name"
                    render={(props) => (
                      <div>
                        <TextInput {...props} placeholder={t("BANK NAME")} disabled />
                      </div>
                    )}
                  />
                </LabelFieldPair>
              </div>
              <div style={{ width: "300px" }}>
                <LabelFieldPair>
                  <CardLabel className="bmc-label">
                    {t("BMC_BRANCH NAME")}
                    {errors.title && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.title.message}</sup>}
                  </CardLabel>
                  <Controller
                    control={control}
                    name="branchName"
                    render={(props) => (
                      <div>
                        <TextInput {...props} placeholder={t("BRANCH NAME")} disabled />
                      </div>
                    )}
                  />
                </LabelFieldPair>
              </div>
            </div>
          </CustomModal>
        </div>
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

export default BankDetailsForm;
