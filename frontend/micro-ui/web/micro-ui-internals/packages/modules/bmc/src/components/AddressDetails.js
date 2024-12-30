import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import isEqual from "lodash.isequal";
import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import ToggleSwitch from "./Toggle";
import MultiColumnDropdown from "./multiColumnDropdown";

const AddressDetailCard = ({ onUpdate, initialRows = {}, AllowEdit = false, tenantId }) => {
  const { t } = useTranslation();
  const [isEditable, setIsEditable] = useState(AllowEdit);
  const headerLocale = useMemo(() => Digit.Utils.locale.getTransformedLocale(tenantId), [tenantId]);
  const pincode = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.pincode || Digit.ULBService.getCurrentTenantId();
  const [options, setOptions] = useState([]);
  const [selectedOptions, setSelectedOptions] = useState([]);

  const RequiredFieldMessage = ({ fieldValue }) => {
    if (!fieldValue || (typeof fieldValue === "object" && !fieldValue.name)) {
      return <p style={{ color: "red", fontSize: "12px", marginTop: "-10px" }}>This is required field *</p>;
    }
    return null;
  };

  Digit.Hooks.bmc.useLocation({ tenantid: tenantId }, "", {
    select: (data) => {
      try {
        if (!data?.BoundaryDetails) {
          console.log("No boundary details found");
          return;
        }

        const mappedOptions = data.BoundaryDetails.map((detail) => ({
          id: detail.id || "",
          pincode: detail.pincode || "",
          district: detail.district || "",
          divisionName: detail.divisionname || "",
          officeName: detail.officename || "",
          stateName: detail.statename || "",
          subWardNo: detail.subwardno || "",
          wardName: detail.wardname || "",
          wardNo: detail.wardno || "",
          blockName: detail.blockname || "",
          zoneName: detail.zonename || "",
        }));
        setOptions(mappedOptions);
      } catch (error) {
        console.error("Error in mapping location data:", error);
      }
    },
  });

  
  const {
    control,
    watch,
    formState: { errors, isValid },
    trigger,
    setValue,
    clearErrors,
    getValues,
  } = useForm({
    defaultValues: {
      house: initialRows?.address?.house || "",
      street: initialRows?.address?.street || "",
      landmark: initialRows?.address?.landmark || "",
      locality: initialRows?.address?.locality || "",
      city: initialRows?.address?.city || "",
      subDistrict: initialRows?.address?.subDistrict || "",
      district: initialRows?.address?.district || "",
      state: initialRows?.address?.state || "",
      zoneName: initialRows?.UserOtherDetails?.zone || "",
      pincode: initialRows?.address?.pincode || "",
      blockName: initialRows?.UserOtherDetails?.block || "",
      wardName: initialRows?.UserOtherDetails?.ward || null,
    },
    mode: "onChange",
  });

  

  const formValuesRef = useRef(getValues());
  const formValues = watch();

  const stableOnUpdate = useCallback(
    (values, valid) => {
      onUpdate(values, valid);
    },
    [onUpdate]
  );

  useEffect(() => {
    if (!isEqual(formValuesRef.current, formValues)) {
      formValuesRef.current = formValues;
      stableOnUpdate(formValues, isValid);
    }
  }, [formValues, isValid, stableOnUpdate]);

  useEffect(() => {
    trigger();
  }, [trigger]);

  useEffect(() => {
    if (initialRows) {
      const dataselected = options.find((item)=>item.pincode == initialRows?.address?.pincode)
      setSelectedOptions(dataselected ? [
        {
          ...dataselected,
          label: dataselected.pincode,
        }
      ] : []);
      setValue("house", initialRows?.address?.house || "");
      setValue("street", initialRows?.address?.street || "");
      setValue("landmark", initialRows?.address?.landmark || "");
      setValue("locality", initialRows?.address?.locality || "");
      setValue("subDistrict", initialRows?.address?.subDistrict || "");
      setValue("district", initialRows?.address?.district || "");
      setValue("state", initialRows?.address?.state || "");
      setValue("city", initialRows?.address?.city || "");
      setValue("pincode", initialRows?.address?.pincode || "");
      setValue("zoneName", initialRows?.UserOtherDetails?.zone || "");
      setValue("wardName", initialRows?.UserOtherDetails?.ward || "");
      setValue("blockName", initialRows?.UserOtherDetails?.block || "");

      if (initialRows?.address?.house) clearErrors("house");
      if (initialRows?.address?.street) clearErrors("street");
      if (initialRows?.address?.landmark) clearErrors("landmark");
      if (initialRows?.address?.locality) clearErrors("locality");
      if (initialRows?.address?.subDistrict) clearErrors("subDistrict");
      if (initialRows?.address?.district) clearErrors("district");
      if (initialRows?.address?.state) clearErrors("state");
      if (initialRows?.address?.city) clearErrors("city");
      if (initialRows?.address?.pincode) clearErrors("pincode");
      if (initialRows?.UserOtherDetails?.zone) clearErrors("zoneName");
      if (initialRows?.UserOtherDetails?.ward) clearErrors("wardName");
      if (initialRows?.UserOtherDetails?.block) clearErrors("blockName");
    }
  }, [initialRows, setValue, headerLocale, clearErrors,options]);

  const handleToggle = () => {
    setIsEditable(!isEditable);
  };

  const handleSelect = (e, selectedOptionsArray) => {
    try {
      const selectedArray = Array.isArray(selectedOptionsArray) ? selectedOptionsArray : [selectedOptionsArray];
      if (selectedArray.length > 0) {
        const selectedDetail = selectedArray[0];
        const updates = {
          pincode: selectedDetail.pincode,
          district: selectedDetail.district || "",
          subDistrict: selectedDetail.officeName || "",
          state: selectedDetail.stateName || "",
          city: selectedDetail.officeName || "",
          wardName: selectedDetail.subWardNo || "",
          zoneName: selectedDetail.divisionName || "",
          blockName: selectedDetail.wardName || "",
        };

        Object.entries(updates).forEach(([key, value]) => {
          setValue(key, value, { shouldValidate: true });
        });
        setSelectedOptions([
          {
            ...selectedDetail,
            label: selectedDetail.pincode,
          },
        ]);

        clearErrors(Object.keys(updates));
      } else {
        resetFieldsToDefault();
      }
    } catch (error) {
      console.error("Error in handleSelect:", error);
    }
  };

  const resetFieldsToDefault = () => {
    const defaultFields = {
      house: "",
      street: "",
      landmark: "",
      locality: "",
      city: "",
      subDistrict: "",
      district: "",
      state: "",
      zoneName: "",
      pincode: "",
      blockName: "",
      wardName: "",
    };

    Object.keys(defaultFields).forEach((key) => {
      setValue(key, defaultFields[key]);
    });

    setSelectedOptions(null);
  };

  return (
    <React.Fragment>
      <form className="bmc-row-card-header">
        <div className="bmc-card-row">
          <span className="bmc-col-large-header">
            <div className="bmc-title">{t("BMC_ADDRESS_DETAILS")}</div>
          </span>
          <span className="bmc-col-small-header" style={{ textAlign: "end" }}>
            <ToggleSwitch
              id={"AddressToggle"}
              isOn={isEditable}
              handleToggle={handleToggle}
              onLabel={t("Editable")}
              offLabel={t("Readonly")}
              disabled={!AllowEdit}
              visible={AllowEdit}
            />
          </span>
        </div>
        <div className="bmc-card-row">
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_HOUSE")}&nbsp;{errors.house && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.house.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name={"house"}
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput
                      disabled={!isEditable}
                      readOnly={!isEditable}
                      value={props.value}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      optionKey="i18nKey"
                      t={t}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_STREET")}&nbsp;{errors.street && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.street.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name={"street"}
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput
                      disabled={!isEditable}
                      readOnly={!isEditable}
                      value={props.value}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      optionKey="i18nKey"
                      t={t}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_LANDMARK")}&nbsp;{errors.landmark && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.landmark.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name={"landmark"}
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput
                      disabled={!isEditable}
                      readOnly={!isEditable}
                      value={props.value}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      optionKey="i18nKey"
                      t={t}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_LOCALITY")}&nbsp;{errors.locality && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.locality.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name={"locality"}
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput
                      disabled={!isEditable}
                      readOnly={!isEditable}
                      value={props.value}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      optionKey="i18nKey"
                      t={t}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
        </div>
        <div className="bmc-card-row">
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_PINCODE")}&nbsp;{errors.wardName && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.wardName.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="pincode"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    {isEditable ? (
                      <MultiColumnDropdown
                        options={options}
                        selected={selectedOptions}
                        onSelect={handleSelect}
                        displayKeys={["pincode", "district", "stateName", "officeName", "subWardNo", "wardName", "divisionName"]}
                        placeholder="Select or Search"
                        optionsKey="value"
                        defaultUnit="Options"
                        autoCloseOnSelect={true}
                        showColumnHeaders={true}
                        headerMappings={{
                          label: t("pincode"),
                          district: t("BMC_DISTRICT"),
                          officeName: t("BMC_SUBDISTRICT"),
                          stateName: t("BMC_STATE"),
                          subWardNo: t("BMC_WARD_NAME"),
                          wardName: t("BMC_BLOCKNAME"),
                          divisionName: t("BMC_ZONENAME"),
                        }}
                      />
                    ) : (
                      <TextInput disabled={!isEditable} readOnly={!isEditable} value={props.value || selectedOptions[0]?.pincode || ""} />
                    )}
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_WARD_NAME")}&nbsp;{errors.wardName && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.wardName.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="wardName"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput disabled readOnly value={props.value || ""} />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">{t("BMC_ZONENAME")}</CardLabel>
              <Controller
                control={control}
                name="zoneName"
                render={(props) => (
                  <div>
                    <TextInput
                      disabled
                      readOnly
                      value={props.value || ""} // Make sure to show the value
                      onChange={(e) => props.onChange(e.target.value)}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">{t("BMC_BLOCKNAME")}</CardLabel>
              <Controller
                control={control}
                name="blockName"
                render={(props) => (
                  <div>
                    <TextInput disabled readOnly value={props.value || ""} />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
        </div>
        <div className="bmc-card-row">
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_CITY")}&nbsp;{errors.city && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.city.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name={"city"}
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput
                      disabled
                      readOnly
                      value={props.value}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      optionKey="i18nKey"
                      t={t}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_SUBDISTRICT")}&nbsp;
                {errors.subDistrict && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.subDistrict.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name={"subDistrict"}
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput
                      disabled
                      readOnly
                      value={props.value}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      optionKey="i18nKey"
                      t={t}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_DISTRICT")}&nbsp;{errors.district && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.district.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name={"district"}
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput
                      disabled
                      readOnly
                      value={props.value}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      optionKey="i18nKey"
                      t={t}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_STATE")}&nbsp;{errors.state && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.state.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name={"state"}
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput
                      disabled
                      readOnly
                      value={props.value}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      optionKey="i18nKey"
                      t={t}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
        </div>
      </form>
    </React.Fragment>
  );
};

export default AddressDetailCard;
