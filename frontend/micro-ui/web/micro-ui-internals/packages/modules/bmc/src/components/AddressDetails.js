import { CardLabel, Dropdown, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import isEqual from "lodash.isequal";
import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import ToggleSwitch from "./Toggle";

const AddressDetailCard = ({ onUpdate, initialRows = {}, AllowEdit = false, tenantId}) => {
  const { t } = useTranslation();
  const [isEditable, setIsEditable] = useState(AllowEdit);
  const headerLocale = useMemo(() => Digit.Utils.locale.getTransformedLocale(tenantId), [tenantId]);
  const pincode = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.pincode || Digit.ULBService.getCurrentTenantId();

  const RequiredFieldMessage = ({ fieldValue }) => {
    if (!fieldValue || (typeof fieldValue === 'object' && !fieldValue.name)) {
      return <p style={{ color: 'red', fontSize: '12px', marginTop: '-10px' }}>This is required field *</p>;
    }
    return null;
  };

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
      zoneName: initialRows?.UserOtherDetails?.zoneName || "",
      pinCode: initialRows?.address?.pinCode || "",
      blockName: initialRows?.UserOtherDetails?.blockName || "",
      wardName: initialRows?.UserOtherDetails?.wardName || null,
    },
    mode: "onChange",
  });

  const [zones, setZones] = useState([]);
  const [blocks, setBlocks] = useState([]);
  const [wards, setWards] = useState([]);

  Digit.Hooks.bmc.useLocation(tenantId, "Zone", {
    select: (data) => {
      const zonesData = [];
      const blocksData = [];
      const wardsData = [];

      data?.TenantBoundary[0]?.boundary.forEach((zone) => {
        zonesData.push({
          code: zone.code,
          name: zone.name,
          i18nKey: `${headerLocale}_ADMIN_${zone.code}`,
        });

        zone.children.forEach((block) => {
          blocksData.push({
            code: block.code,
            name: block.name,
            zoneCode: zone.code,
            i18nKey: `${headerLocale}_ADMIN_${block.code}`,
          });

          block.children.forEach((ward) => {
            wardsData.push({
              code: ward.code,
              name: ward.name,
              zoneCode: zone.code,
              blockCode: block.code,
              i18nKey: `${headerLocale}_ADMIN_${ward.code}`,
            });
          });
        });
      });

      setZones(zonesData);
      setBlocks(blocksData);
      setWards(wardsData);

      return {
        zonesData,
        blocksData,
        wardsData,
      };
    },
  });

  const selectedWard = watch("wardName");
  const [filteredBlocks, setFilteredBlocks] = useState([]);
  const [filteredZones, setFilteredZones] = useState([]);

  useEffect(() => {
    if (selectedWard && selectedWard.code) {
      const selectedBlockCode = wards.find((ward) => ward.code === selectedWard.code)?.blockCode;
      const selectedZoneCode = wards.find((ward) => ward.code === selectedWard.code)?.zoneCode;

      if (selectedBlockCode) {
        const filteredBlocks = blocks.filter((block) => block.code === selectedBlockCode);
        setFilteredBlocks(filteredBlocks);
        setValue("blockName", filteredBlocks[0]?.name || ""); // Automatically set the block name text field
      } else {
        setFilteredBlocks([]);
        setValue("blockName", ""); // Reset block text field
      }

      if (selectedZoneCode) {
        const filteredZones = zones.filter((zone) => zone.code === selectedZoneCode);
        setFilteredZones(filteredZones);
        setValue("zoneName", filteredZones[0]?.name || ""); // Automatically set the zone name text field
      } else {
        setFilteredZones([]);
        setValue("zoneName", ""); // Reset zone text field
      }
    } else {
      setFilteredBlocks([]);
      setFilteredZones([]);
    }
  }, [wards, blocks, zones, selectedWard, setValue]);

  useEffect(() => {
    if (!selectedWard) {
      setFilteredBlocks([]);
      setFilteredZones([]);
      setValue("blockName", ""); // Reset block text field
      setValue("zoneName", ""); // Reset zone text field
    }
  }, [selectedWard, setValue]);

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
    trigger(); // Validate the form on mount to show errors if fields are empty
  }, [trigger]);

  useEffect(() => {
    if (initialRows) {
      setValue("house", initialRows?.address?.house || "");
      setValue("street", initialRows?.address?.street || "");
      setValue("landmark", initialRows?.address?.landmark || "");
      setValue("locality", initialRows?.address?.locality || "");
      setValue("subDistrict", initialRows?.address?.subDistrict || "");
      setValue("district", initialRows?.address?.district || "");
      setValue("state", initialRows?.address?.state || "");

      setValue("city", initialRows?.address?.city || "");
      setValue("pinCode", initialRows?.address?.pinCode || "");

      const zonedata = zones.find((zone) => zone.code === initialRows?.UserOtherDetails?.zone) || "";
      const blockdata = blocks.find((block) => block.code === initialRows?.UserOtherDetails?.block) || "";
      const warddata = wards.find((ward) => ward.code === initialRows?.UserOtherDetails?.ward) || "";

      setValue("zoneName", zonedata?.name || "");
      setValue("blockName", blockdata?.name || "");
      setValue("wardName", warddata || null);

      if (initialRows?.address?.house) clearErrors("house");
      if (initialRows?.address?.street) clearErrors("street");
      if (initialRows?.address?.landmark) clearErrors("landmark");
      if (initialRows?.address?.locality) clearErrors("locality");
      if (initialRows?.address?.subDistrict) clearErrors("subDistrict");
      if (initialRows?.address?.district) clearErrors("district");
      if (initialRows?.address?.state) clearErrors("state");
      if (initialRows?.address?.city) clearErrors("city");
      if (initialRows?.address?.pinCode) clearErrors("pinCode");
      if (zonedata) clearErrors("zoneName");
      if (blockdata) clearErrors("blockName");
      if (warddata) clearErrors("wardName");
    }
  }, [initialRows, setValue, headerLocale, clearErrors, zones, blocks, wards]);

  const handleToggle = () => {
    setIsEditable(!isEditable);
  };

  const pincodeOptions = (Array.isArray(pincode) ? pincode : []).map((pin) => ({
    value: pin,
  }));

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
              <CardLabel className="bmc-label">{t("BMC_HOUSE")}&nbsp;{errors.house && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.house.message}</sup>}</CardLabel>
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
              <CardLabel className="bmc-label">{t("BMC_STREET")}&nbsp;{errors.street && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.street.message}</sup>}</CardLabel>
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
              <CardLabel className="bmc-label">{t("BMC_LANDMARK")}&nbsp;{errors.landmark && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.landmark.message}</sup>}</CardLabel>
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
              <CardLabel className="bmc-label">{t("BMC_LOCALITY")}&nbsp;{errors.locality && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.locality.message}</sup>}</CardLabel>
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
              <CardLabel className="bmc-label">{t("BMC_CITY")}&nbsp;{errors.city && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.city.message}</sup>}</CardLabel>
              <Controller
                control={control}
                name={"city"}
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
              <CardLabel className="bmc-label">{t("BMC_SUBDISTRICT")}&nbsp;{errors.subDistrict && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.subDistrict.message}</sup>}</CardLabel>
              <Controller
                control={control}
                name={"subDistrict"}
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
              <CardLabel className="bmc-label">{t("BMC_DISTRICT")}&nbsp;{errors.district && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.district.message}</sup>}</CardLabel>
              <Controller
                control={control}
                name={"district"}
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
              <CardLabel className="bmc-label">{t("BMC_STATE")}&nbsp;{errors.state && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.state.message}</sup>}</CardLabel>
              <Controller
                control={control}
                name={"state"}
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
              <CardLabel className="bmc-label">{t("BMC_PINCODE")}&nbsp;{errors.pinCode && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.pinCode.message}</sup>}</CardLabel>
              <Controller
                control={control}
                name="pinCode"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    {isEditable ? (
                      <Dropdown
                        placeholder={t("SELECT_PINCODE")}
                        option={pincodeOptions}
                        selected={props.value}
                        select={(pincode) => props.onChange(pincode)}
                        onBlur={props.onBlur}
                        optionKey="value"
                        t={t}
                        isMandatory={true}
                        className="employee-select-wrap bmc-form-field"
                      />
                    ) : (
                      <TextInput disabled={!isEditable} readOnly={!isEditable} value={props?.value?.value} />
                    )}
                                        {/* <RequiredFieldMessage fieldValue={props.value} /> */}

                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">{t("BMC_WARD_NAME")}&nbsp;{errors.wardName && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.wardName.message}</sup>}</CardLabel>
              <Controller
                control={control}
                name="wardName"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    {isEditable ? (
                      <Dropdown
                        placeholder={t("SELECT SUBWARD")}
                        selected={props.value}
                        select={(ward) => props.onChange(ward)}
                        onBlur={props.onBlur}
                        option={wards}
                        optionKey="name"
                        t={t}
                        isMandatory={true}
                        className="employee-select-wrap bmc-form-field"
                      />
                    ) : (
                      <TextInput disabled={!isEditable} readOnly={!isEditable} value={props.value?.name || ""} />
                    )}

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
                    <TextInput disabled readOnly value={props.value || ""} />
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
      </form>
    </React.Fragment>
  );
};

export default AddressDetailCard;
