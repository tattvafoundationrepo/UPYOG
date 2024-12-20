import React, { useState, useMemo, useEffect } from "react";
import { useTranslation } from "react-i18next";
import CustomTable from "./CustomTable";
import CustomModal from "./CustomModal";

import { Controller, useForm } from "react-hook-form";
import { TextInput, Dropdown, CardLabel, LabelFieldPair, RemoveIcon, Toast } from "@upyog/digit-ui-react-components";
import TableCard from "@tattvafoundation/digit-ui-module-deonar/src/pages/employee/commonFormFields/tableCard";

const DocumentCard = ({ tenantId, onUpdate, initialRows = [], AllowEdit = true, AddOption = true, AllowRemove = true, isLoading }) => {
  const { t } = useTranslation();
  const headerLocale = Digit.Utils.locale.getTransformedLocale(tenantId);
  const userDetails = Digit.UserService.getUser();

  const [documents, setDocuments] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [totalRecords, setTotalRecords] = useState(0);
  const [sortParams, setSortParams] = useState({});
  const [userDetail, setUserDetail] = useState([]);

  const [isEditing, setIsEditing] = useState(false);
  const [selectedRow, setSelectedRow] = useState(null);

  const [isMobileView, setIsMobileView] = useState(window.innerWidth < 769);
  const [toast, setToast] = useState('')

  const initialDefaultValues = useMemo(
    () => ({
      document: null,
      documentNo: "",
    }),
    []
  );

  const toggleModal = () => setIsModalOpen(!isModalOpen);

  const processDocumentData = (items, headerLocale) => {
    if (items.length === 0) return [];
    return items
      .map((item) => {
        if (typeof item === "object" && item.id && item.name) {
          return {
            document: {
              id: item.id,
              document: item.name,
              i18nKey: `${headerLocale}_ADMIN_${item.name}`,
            },
            documentNo: item.documentNo,
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
        document: item.name,
        i18nKey: `${headerLocale}_ADMIN_${item.name}`,
      })) || []
    );
  };

  const documentFunction = (data) => {
    const documentsData = processCommonData(data, headerLocale);
    setDocuments(documentsData);
    return { documentsData };
  };

  const getDocuments = { CommonSearchCriteria: { Option: "document" } };
  Digit.Hooks.bmc.useCommonGet(getDocuments, { select: documentFunction });

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
    const processedRows = processDocumentData(initialRows, headerLocale);
    if (processedRows.length === 0) {
    } else {
      setRows(processedRows);
    }
  }, [initialRows, headerLocale, initialDefaultValues]);

  const removeRows = Digit.Hooks.bmc.useRemoveDocuments();

  const removeDocumentRow = (id) => {
    const documentToRemove = rows.find((item) => item.document.id === id);
  
    if (!documentToRemove) {
      return;
    }
  
    const payload = {
      removalcriteria: {
        id: documentToRemove.document.id,
        option: "document",
      },
    };
  
    removeRows.mutate(payload, {
      onSuccess: () => {
        setRows((prevRows) => prevRows.filter((row) => row.document.id !== id));
  
        setToastWithTimeout("success", t("DOCUMENT REMOVED SUCCESSFULLY"));
      },
      onError: (error) => {
        console.error("Failed to remove document:", error);
  
        setToastWithTimeout("error", t("FAILED TO REMOVE DOCUMENT. PLEASE TRY AGAIN!"));
      },
    });
  };

  const saveDocumentData = Digit.Hooks.bmc.useSaveDocument();

  const addRow = () => {
    const formData = getValues();

    const isDuplicate = rows.some((row) => row.document.document === formData.document.document);

    if (isDuplicate) {
      alert(t("Document type already exists. Please select a different document."));
      return;
    }

    const newRow = {
      updatedDocument: [
        {
          document: formData.document || "",
          documentNo: formData.documentNo || "",
        },
      ],
    };

    saveDocumentData.mutate(newRow, {
      onSuccess: (response) => {

        
        setRows((prevRows) => [
          ...prevRows,
          ...newRow.updatedDocument.map((doc) => ({
            document: doc.document,
            documentNo: doc.documentNo,
          })),

        ]);
        
        setToastWithTimeout("success", t("DOCUMENT ADDED SUCCESSFULLY"));


        toggleModal();

        reset(initialDefaultValues);

        if (onUpdate) {
          onUpdate([
            ...rows,
            ...newRow.updatedDocument.map((doc) => ({
              document: doc.document.document,
              documentNo: doc.documentNo,
            })),
          ]);
          
        }
      },
      onError: (error) => {
        console.error("Failed to save document:", error);
        setToastWithTimeout("error", t("Please fill all the required fields !"));

      },
    });
  };
  const setToastWithTimeout = (key, action) => {
    setToast({ key, action });
  
    setTimeout(() => {
      setToast(null);
    }, 3000);
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
      Header: t("BMC_DOCUMENT"),
      accessor: t("document"),
      sortable: true,
      Cell: ({ row }) => (
        <span
          onClick={AllowEdit ? () => handleOnClick(row.original.document.document) : null}
          style={{
            cursor: AllowEdit ? "pointer" : "default",
            color: AllowEdit ? "red" : "black",
          }}
        >
          {t(row.original.document.i18nKey) || row.original.document.document}
        </span>
      ),
      isVisible: true,
    },
    { Header: t("BMC_DOCUMENTNO"), accessor: "documentNo" },
    {
      Header: t(t("Actions")),
      accessor: "action",
      Cell: ({ row }) => (
        <span
          onClick={AllowRemove ? () => removeDocumentRow(row.original.document.id) : null}
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
      key: "document",
      label: t("BMC_ADD_DOCUMENT"),
      display: (data) => t(data.document?.i18nKey) || "N/A", // Safely access nested value
    },
    {
      key: "documentNo",
      label: t("BMC_DOCUMENTNO"),
    },
    {
      key: "action",
      label: t("Actions"),
      display: (data) => (
        <button
          onClick={() => removeDocumentRow(data.document?.id)}
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

  const myConfig = {
    elements: [
      {
        label: { heading: t("BMC_ADD_DOCUMENT"), cancel: "Cancel", submit: "Submit" },
        type: "p",
        text: t("BMC_ADD_DOCUMENT"),
        style: { textDecoration: "underline", cursor: "pointer" },
        onClick: "onAddDocumentClick",
      },
      {
        label: { heading: "Add Document Number", cancel: "Cancel", submit: "Submit" },
      },
    ],
  };

  const handleAddEmployee = () => {
    setIsModalOpen(!isModalOpen);
  };

  const [setUuid, setSetUuid] = useState("");

  const handleOnClick = (rowDocument) => {
    if (!AllowEdit) return;

    const rowToEdit = rows.find((row) => row.document.document === rowDocument);
    if (rowToEdit) {
      setSelectedRow(rowToEdit);
      setIsEditing(true);
      reset({
        document: rowToEdit.document,
        documentNo: rowToEdit.documentNo,
      });
      toggleModal();
    }
  };

  const updateRow = () => {
    const formData = getValues();
    setRows((prevRows) =>
      prevRows.map((row) =>
        row.document.document === selectedRow.document.document
          ? {
              ...row,
              document: formData.document,
              documentNo: formData.documentNo,
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
    <div>
      <div className="bmc-card-row">
        <div className="bmc-row-card-header">
          <div className="bmc-title">{t("BMC_DOCUMENT_DETAILS")}</div>
          {isMobileView && rows.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)}
          {!isMobileView && (
            <CustomTable
              t={t}
              columns={visibleColumns(handleOnClick)}
              data={rows || []}
              manualPagination={false}
              totalRecords={totalRecords}
              sortParams={sortParams}
              onAddEmployeeClick={handleAddEmployee}
              config={myConfig}
              tableClassName={"ebe-custom-scroll"}
              isLoadingRows={isLoading}
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
            title={<h1 style={{marginLeft:'0px'}} className="heading-m">{isEditing ? t("Edit Document Details") : t("Add Document Details")}</h1>}
            actionCancelLabel={t("Cancel")}
            actionCancelOnSubmit={() => {
              toggleModal();
              setIsEditing(false);
              reset(initialDefaultValues);
            }}
            actionSaveLabel={t(isEditing && AllowEdit ? t("Update") : t("Submit"))}
            actionSaveOnSubmit={handleSubmit(isEditing && AllowEdit ? updateRow : addRow)}
            formId="modal-action"
          >
            <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }} className="bmc-col11-card">
              <div style={{ width: "300px" }}>
                <LabelFieldPair>
                  <CardLabel className="bmc-label">
                    {t("BMC_DOCUMENT")}
                    {errors.document && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.document.message}</sup>}
                  </CardLabel>

                  <Controller
                    control={control}
                    name="document"
                    rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                    render={(props) => (
                      <div>
                        <Dropdown
                          placeholder={t("SELECT THE DOCUMENT")}
                          selected={props.value}
                          select={(document) => props.onChange(document)}
                          option={documents}
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
                    {t("BMC_DOCUMENTNO")}
                    {errors.documentNo && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.documentNo.message}</sup>}
                  </CardLabel>
                  <Controller
                    control={control}
                    name="documentNo"
                    rules={{
                      required: t("CORE_COMMON_REQUIRED_ERRMSG"),
                    }}
                    render={(props) => (
                      <div>
                        <TextInput placeholder={t("BMC_DOCUMENTNO")} value={props.value} onChange={props.onChange} onBlur={props.onBlur} t={t} />
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
    </div>
  );
};

export default DocumentCard;
