import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import MainFormHeader from "../commonFormFields/formMainHeading";
import CustomTable from "../commonFormFields/customTable";
import TableCard from "../commonFormFields/tableCard";
import { CardLabel, LabelFieldPair, Toast, Dropdown, CheckBox, ToggleSwitch } from "@upyog/digit-ui-react-components";
import { useForm } from "react-hook-form";
import useCollectionPoint from "@upyog/digit-ui-libraries/src/hooks/deonar/useCollectionPoint";
import SubmitButtonField from "../commonFormFields/submitBtn";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import { generateTokenNumber } from "../collectionPoint/utils";
import CustomModal from "../commonFormFields/customModal";
import MultiColumnDropdown from "../commonFormFields/multiColumnDropdown";

const Slaughtering = () => {
  const { t } = useTranslation();
  const [slaughterList, setSlaughterList] = useState([]);
  const [slaughterAnimalListData, setSlaughterAnimalListData] = useState([]);
  const [selectedUUID, setSelectedUUID] = useState(null);
  const [toast, setToast] = useState(null);
  const [totalRecords, setTotalRecords] = useState(0);
  const [isMobileView, setIsMobileView] = useState(window.innerWidth < 768);
  const [selectedRows, setSelectedRows] = useState([]);
  const [slaughteringUnit, setSlaughteringUnit] = useState([]);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [slaughterAssignment, setSlaughterAssignment] = useState([]);
  const [selectedSlaughterUnits, setSelectedSlaughterUnits] = useState({});

  const [slaughterDates, setSlaughterDates] = useState({});

  const [shiftOptions, setShiftOptions] = useState({});
  const [selectedShifts, setSelectedShifts] = useState({});
  const [slaughterByBmc, setSlaughterByBmc] = useState({});
  const [selectAll, setSelectAll] = useState(false);

  const [uniqueSlaughterUnits, setUniqueSlaughterUnits] = useState([]);
  const [shiftsMapping, setShiftsMapping] = useState({});
  const [unitIdMapping, setUnitIdMapping] = useState({});

  const [selectedUnitForFetch, setSelectedUnitForFetch] = useState(null);
  const { fetchSlaughterCollectionList } = useCollectionPoint({});
  const { data: SlaughterListData } = fetchSlaughterCollectionList({}, { executeOnLoad: true });
  const { saveSlaughterListData, fetchDeonarCommon, fetchSlaughterUnit, saveSlaughterList } = useDeonarCommon();

  const { data: slaughterUnitData, refetch } = fetchSlaughterUnit(
    {},
    {
      executeOnLoad: true,
    }
  );

  useEffect(() => {
    const unitId = selectedUnitForFetch;
    setSelectedUnitForFetch(unitId);
  }, []);

  useEffect(() => {
    if (slaughterUnitData?.unit) {
      const mappedShifts = slaughterUnitData.unit.map((unit) => ({
        label: `${unit.openTime} - ${unit.closeTime}`,
        value: unit.id,
      }));

      // Set shifts for all rows
      const allRowsShifts = {};
      slaughterAssignment.forEach((_, index) => {
        allRowsShifts[index] = mappedShifts;
      });

      setShiftOptions(allRowsShifts);
    }
  }, [slaughterUnitData, slaughterAssignment]);

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    reset,
    formState: { errors, isDirty },
  } = useForm({
    defaultValues: {
      slaughter: "",
      slaughterType: {},
      slaughterUnit: {},
    },
  });

  const handleUUIDClick = (ddReference, entryUnitId) => {
    setSelectedUUID(entryUnitId);
    setIsModalOpen(!isModalOpen);
    setSelectedUUID(ddReference);
    const selectedSlaughter = slaughterList.find((item) => item.ddReference === ddReference);
    if (selectedSlaughter) {
      setValue("arrivalId", selectedSlaughter.arrivalId);
      setValue("animalType", selectedSlaughter.animalAssignmentDetailsList?.animalType || "");
      setValue("token", selectedSlaughter.animalAssignmentDetailsList?.token || "");
      setValue("slaughter", selectedSlaughter.animalAssignmentDetailsList?.slaughtering || "");

      const updatedAnimalList =
        selectedSlaughter.animalAssignmentDetailsList?.map((item) => ({
          ...item,
          animalTokenNumber: generateTokenNumber(item.animalType, item.token),
        })) || [];
      setSlaughterAssignment(updatedAnimalList || []);
    }
  };

  const saveSlaughterData = saveSlaughterListData();

  const showToast = (type, message, duration = 5000) => {
    setToast({ key: type, action: message });
    setTimeout(() => {
      setToast(null);
    }, duration);
  };

  const handleToggleChange = (rowIndex) => (e) => {
    const newValue = !slaughterByBmc[rowIndex];
    setSlaughterByBmc((prev) => ({
      ...prev,
      [rowIndex]: newValue,
    }));

    // Check if all toggles are selected after this change
    const updatedValues = {
      ...slaughterByBmc,
      [rowIndex]: newValue,
    };

    const allSelected = Object.values(updatedValues).every((value) => value === true);
    setSelectAll(allSelected);
  };

  useEffect(() => {
    if (Object.keys(slaughterByBmc).length > 0) {
      const allSelected = Object.values(slaughterByBmc).every((value) => value === true);
      setSelectAll(allSelected);
    }
  }, [slaughterByBmc]);

  const handleSelectAll = (e) => {
    const isChecked = e.target.checked;
    setSelectAll(isChecked);
    const updatedSlaughterByBmc = slaughterAssignment.reduce((acc, _, index) => {
      acc[index] = isChecked;
      return acc;
    }, {});

    setSlaughterByBmc(updatedSlaughterByBmc);
  };

  const resetModal = () => {
    setSelectedUUID(undefined);
  };

  const onSubmit = async (formData) => {
    try {
      const selectedSlaughter = slaughterList.find((item) => item.ddReference === selectedUUID);

      if (!selectedSlaughter) {
        showToast("error", t("No selected record found"));
        return;
      }

      const bookingDetails = slaughterAssignment
        .map((item, index) => {
          const slaughterUnit = selectedSlaughterUnits[index]?.[0];
          const selectedShift = selectedShifts[index];
          const date = slaughterDates[index];

          // Only proceed if we have all required data
          if (!slaughterUnit || !selectedShift || !date) return null;

          return {
            arrivalId: selectedSlaughter.arrivalId,
            ddReference: selectedSlaughter.ddReference,
            animalTypeId: item.animalTypeId,
            token: item.token,
            slaughterUnitId: unitIdMapping[slaughterUnit.value], // Using the correct unit ID from mapping
            unitShiftId: selectedShift.value, // Using the shift ID
            slaughterDate: date ? new Date(date).getTime() : null,
            slaughterByBmcEmployee: slaughterByBmc[index] || false,
          };
        })
        .filter(Boolean); // Remove any null entries

      console.log("Sending payload:", bookingDetails); // For debugging

      const payload = {
        bookingDetails,
      };

      saveSlaughterList(payload, {
        onSuccess: () => {
          reset();
          resetForm();
          setSelectedRows([]);
          setSelectedShifts({});
          setSelectedSlaughterUnits({});
          setSlaughterByBmc({});
          showToast("success", t("DEONAR_SLAUGHTERING_DATA_SUBMITTED_SUCCESSFULY"));
          setIsSubmitted(true);
          setIsModalOpen(false);
        },
        onError: (error) => {
          console.error("Submission error:", error);
          showToast("error", t("DEONAR_SLAUGHTERING_DATA_NOT_SUBMITTED_SUCCESSFULY"));
        },
      });
    } catch (error) {
      console.error("Form submission error:", error);
      showToast("error", t("DEONAR_SLAUGHTERING_DATA_NOT_SUBMITTED_SUCCESSFULY"));
    }
  };

  useEffect(() => {
    if (SlaughterListData) {
      setSlaughterList(SlaughterListData?.SlaughterList || []);
      setTotalRecords(SlaughterListData?.SlaughterList?.length);
      const transformedData = SlaughterListData.SlaughterList.map((item) => ({
        animalType: item.animalAssignmentDetailsList?.[0]?.animalType || "Unknown",
        animalTypeId: item.animalAssignmentDetailsList?.[0]?.animalTypeId || null,
        token: item.animalAssignmentDetailsList?.[0]?.token || null,
      }));
      setSlaughterAssignment(transformedData);
    }
  }, [SlaughterListData]);

  useEffect(() => {
    if (slaughterUnitData?.unit) {
      // Group units by name
      const unitsByName = slaughterUnitData.unit.reduce((acc, unit) => {
        if (!acc[unit.name]) {
          acc[unit.name] = [];
        }
        acc[unit.name].push(unit);
        return acc;
      }, {});

      // Store the first unit ID for each unique name
      const idMapping = {};
      Object.entries(unitsByName).forEach(([name, units]) => {
        idMapping[name] = units[0].id; // Store the first unit's ID for each unique name
      });
      setUnitIdMapping(idMapping);

      // Create unique units array for first dropdown
      const uniqueUnits = Object.keys(unitsByName).map((name) => ({
        label: name,
        value: name,
      }));
      setUniqueSlaughterUnits(uniqueUnits);

      // Create shifts mapping for each unique unit name
      const shiftsMap = {};
      Object.entries(unitsByName).forEach(([unitName, units]) => {
        shiftsMap[unitName] = units.map((unit) => ({
          label: `${unit.openTime} - ${unit.closeTime}`,
          value: unit.id,
        }));
      });
      setShiftsMapping(shiftsMap);
    }
  }, [slaughterUnitData]);

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  const resetForm = () => {
    setSelectedShifts({});
    setShiftOptions({});
    setSelectedSlaughterUnits({});
    setSlaughterDates({}); // Reset dates
    reset();
  };

  const Tablecolumns = [
    {
      Header: t("ID"),
      accessor: "id",
      Cell: ({ row }) => row.index + 1,
      isVisible: false,
    },
    {
      Header: t("Deonar_DD_Reference"),
      accessor: "ddReference",
      Cell: ({ row }) => (
        <span onClick={() => handleUUIDClick(row.original.ddReference)} style={{ cursor: "pointer", color: "blue" }}>
          {row.original.ddReference}
        </span>
      ),
    },
    {
      Header: t("DEONAR_SHOPKEEPER_NAME"),
      accessor: "shopkeeperName",
    },
    {
      Header: t("DEONAR_LICENSE_NUMBER"),
      accessor: "licenceNumber",
    },
    {
      Header: t("DEONAR_ARRIVAL_UUID"),
      accessor: "arrivalId",
    },
  ];

  const toggleModal = () => {
    if (isDirty) {
      setIsConfirmModalOpen(true);
    } else {
      resetModal();
      setIsModalOpen(false);
      resetForm();
    }
  };

  const getTodayDate = () => {
    const today = new Date();
    return today.toISOString().split("T")[0];
  };

  const handleDateChange = (rowIndex, date) => {
    const selectedDate = new Date(date);
    const today = new Date(getTodayDate());

    if (selectedDate >= today) {
      setSlaughterDates((prev) => ({
        ...prev,
        [rowIndex]: date,
      }));
    } else {
      showToast("error", t("DEONAR_DATE_CANNOT_BE_BEFORE_TODAY"));
      // Clear the invalid date
      setSlaughterDates((prev) => ({
        ...prev,
        [rowIndex]: "",
      }));
    }
  };

  const handleSlaughterUnitSelect = (rowIndex, selectedOptions) => {
    const formattedSelection = Array.isArray(selectedOptions) ? selectedOptions : [selectedOptions];
    setSelectedSlaughterUnits((prev) => ({
      ...prev,
      [rowIndex]: formattedSelection,
    }));
    setSelectedShifts((prev) => ({
      ...prev,
      [rowIndex]: null,
    }));
  };

  const handleShiftSelect = (rowIndex, selected) => {
    const selectedShift = Array.isArray(selected) ? selected[0] : selected;
    setSelectedShifts((prev) => ({
      ...prev,
      [rowIndex]: selectedShift,
    }));
  };

  const isVisibleColumns = [
    { Header: t("Animal Type"), accessor: "animalType" },
    {
      Header: t("Animal Token"),
      accessor: "token",
      Cell: ({ row }) => {
        const animalType = row.original.animalType;
        const index = row.index;
        return generateTokenNumber(animalType, row.original.token);
      },
    },
    {
      accessor: t("animalDetails"),
      Header: t("Assign Slaughtering Unit"),

      Cell: ({ row }) => {
        const rowIndex = row.index;
        const selectedValue = selectedSlaughterUnits[rowIndex] || [];
        return (
          <MultiColumnDropdown
            key={`slaughter-unit-${rowIndex}`}
            options={uniqueSlaughterUnits || []}
            selected={selectedValue}
            onSelect={(e, selected) => handleSlaughterUnitSelect(rowIndex, selected)}
            placeholder={t("Select Assignment Unit")}
            displayKeys={["label"]}
            optionsKey="value"
            defaultUnit="Options"
            autoCloseOnSelect={true}
            showColumnHeaders={true}
            headerMappings={{
              label: t("name"),
            }}
          />
        );
      },
    },
    {
      accessor: t("slaughterShift"),
      Header: t("Assign Shift"),
      Cell: ({ row }) => {
        const rowIndex = row.index;
        const selectedUnit = selectedSlaughterUnits[rowIndex]?.[0];
        const availableShifts = selectedUnit ? shiftsMapping[selectedUnit.value] : [];
        const selectedShift = selectedShifts[rowIndex];

        return (
          <MultiColumnDropdown
            key={`slaughter-shift-dropdown-${rowIndex}`}
            options={availableShifts || []}
            selected={selectedShift ? [selectedShift] : []}
            onSelect={(e, selected) => handleShiftSelect(rowIndex, selected)}
            placeholder={t("Select Shift")}
            displayKeys={["label"]}
            optionsKey="value"
            defaultUnit="Options"
            autoCloseOnSelect={true}
            showColumnHeaders={true}
            headerMappings={{
              label: t("name"),
            }}
          />
        );
      },
    },

    {
      accessor: "slaughterDate",
      Header: t("Slaughter Booking Date"),
      Cell: ({ row }) => (
        <input
          type="date"
          style={{ border: "1px solid ", padding: "3px", background: "rgba(227, 227, 227, var(--bg-opacity))" }}
          value={slaughterDates[row.index] || ""}
          onChange={(e) => handleDateChange(row.index, e.target.value)}
          min={getTodayDate()} 
          className="digit-datepicker"
        />
      ),
    },

    {
      accessor: "slaughterByBmcEmployee",
      Header: () => (
        <div className="flex items-center justify-center">
          <CheckBox label={t("Slaughter by BMC Employee")} onChange={handleSelectAll} checked={selectAll} style={{ margin: "0 auto", top: "3px"}} />
        </div>
      ),
      Cell: ({ row }) => {
        const rowIndex = row.index;
        return (
          <div className="flex items-center justify-center" style={{ paddingBottom: "15px" }}>
            <ToggleSwitch
              value={slaughterByBmc[rowIndex] || false}
              onChange={handleToggleChange(rowIndex)}
              name={`toggle-${rowIndex}`}
              style={{ margin: "0 auto" }}
            />
          </div>
        );
      },
    },
  ];

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={t("DEONAR_SLAUGHTERING")} />
          <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
            <div className="bmc-row-card-header">
              {isMobileView &&
                slaughterList.map((data, index) => (
                  <TableCard
                    data={data}
                    key={index}
                    fields={[
                      { key: "ddReference", label: t("Deonar_DD_Reference"), isClickable: true },
                      { key: "shopkeeperName", label: t("DEONAR_TRADER_NAME") },
                      { key: "licenceNumber", label: t("DEONAR_LICENSE_NUMBER") },
                      { key: "arrivalId", label: t("DEONAR_ARRIVAL_UUID") },
                    ]}
                    onUUIDClick={handleUUIDClick}
                  />
                ))}
              <CustomTable
                t={t}
                columns={Tablecolumns}
                data={slaughterList}
                manualPagination={false}
                tableClassName={"deonar-scrollable-table"}
                totalRecords={totalRecords}
                autoSort={false}
              />
            </div>
          </div>

          {isModalOpen && (
            <CustomModal isOpen={isModalOpen} onClose={toggleModal} selectedUUID={selectedUUID} style={{ width: "100%" }}>
              {/* <div className="bmc-row-card-header" style={{ marginBottom: "40px" }}> */}

              {/* </div> */}
              <div className="bmc-card-row">
                {/* <div className="bmc-row-card-header" style={{ overflowY: "auto", maxHeight: "300px" }}> */}
                {/* {isMobileView && data.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)} */}
                <CustomTable
                  t={t}
                  columns={isVisibleColumns}
                  manualPagination={false}
                  data={slaughterAssignment}
                  totalRecords={totalRecords}
                  tableClassName={"deonar-scrollable-table"}
                  autoSort={false}
                  isLoadingRows={""}
                  showDateColumn={false}
                  // onDateChange={handleDateChange}
                />
                {/* </div> */}
              </div>
              <SubmitButtonField control={control} />
            </CustomModal>
          )}
        </form>
      </div>
      {toast && (
        <Toast
          error={toast.key === t("error")}
          label={t(toast.key === t("success") ? t("DEONAR_SLAUGHTERING_DATA_SUBMITTED_SUCCESSFULY") : toast.action)}
          onClose={() => setToast(null)}
          style={{ maxWidth: "670px" }}
        />
      )}
    </React.Fragment>
  );
};

export default Slaughtering;
