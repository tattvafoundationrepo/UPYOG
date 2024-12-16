import React, { useState, useEffect } from "react";
import { Controller } from "react-hook-form";
import { Header, LabelFieldPair, CardLabel, Dropdown, TextInput } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import CustomModal from "../commonFormFields/customModal";
export const AnimalInspectionModal = ({
  isModalOpen,
  toggleModal,
  control,
  inspectionTableData,
  speciesOptions,
  breedOptions,
  sexOptions,
  approxAgeOptions,
  bodyColorOptions,
  pregnancyOptions,
  opinionOptions,
  eyesOptions,
  nostrilOptions,
  muzzleOptions,
  bodyTempOptions,
  pulseOptions,
  postureOptions,
  gaitOptions,
  appetiteOptions,
  handleUpdateValue,
  selectedAnimal,
}) => {
  const { t } = useTranslation();
  const [localData, setLocalData] = useState(selectedAnimal);

  useEffect(() => {
    if (selectedAnimal) {
      setLocalData(selectedAnimal);
    }
  }, [selectedAnimal]);

  const handleChange = (field, value) => {
    setLocalData((prevData) => ({
      ...prevData,
      [field]: value,
    }));
  };

  const handleSave = () => {
    handleUpdateValue(localData);
    toggleModal();
  };

  if (!selectedAnimal) return null;

  return (
    <CustomModal isOpen={isModalOpen} onClose={toggleModal} fullScreen>
      <form>
        <React.Fragment>
          <div className="bmc-card-row">
            <div style={{ paddingBottom: "20px", display: "flex", gap: "12px", alignItems: "center" }}>
              <h3 style={{ fontWeight: "600", fontSize: "20px" }}>{t("Active Animal Token Number")}: </h3>
              <span
                style={{
                  fontWeight: "bold",
                  backgroundColor: "rgb(204, 204, 204)",
                  borderRadius: "10px",
                  padding: "8px",
                  fontSize: "22px",
                }}
              >
                {localData?.animal}
              </span>
            </div>
            <Header>{t("Animal_Characterstics_General")}</Header>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_SPECIES")}</CardLabel>
                <Controller
                  control={control}
                  name="species"
                  render={(props) => (
                    <Dropdown
                      option={speciesOptions}
                      select={(value) => {
                        handleChange("species", value);
                        props.onChange(value);
                      }}
                      selected={localData?.species}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_SPECIES")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_BREED")}</CardLabel>
                <Controller
                  control={control}
                  name="breed"
                  render={(props) => (
                    <Dropdown
                      option={breedOptions}
                      select={(value) => {
                        handleChange("breed", value);
                        props.onChange(value);
                      }}
                      selected={localData?.breed}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_BREED")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_SEX")}</CardLabel>
                <Controller
                  control={control}
                  name="sex"
                  render={(props) => (
                    <Dropdown
                      option={sexOptions}
                      select={(value) => {
                        handleChange("sex", value);
                        props.onChange(value);
                      }}
                      selected={localData?.sex}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_SEX")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_APPROXIMATE_AGE")}</CardLabel>
                <Controller
                  control={control}
                  name="approxAge"
                  render={(props) => (
                    <Dropdown
                      option={approxAgeOptions}
                      select={(value) => {
                        handleChange("approxAge", value);
                        props.onChange(value);
                      }}
                      selected={localData?.approxAge}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_APPROXIMATE_AGE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-card-row">
              <div className="bmc-col3-card">
                <LabelFieldPair>
                  <CardLabel className="bmc-label">{t("DEONAR_BODY_COLOR")}</CardLabel>
                  <Controller
                    control={control}
                    name="bodyColor"
                    render={(props) => (
                      <Dropdown
                        option={bodyColorOptions}
                        select={(value) => {
                          handleChange("bodyColor", value);
                          props.onChange(value);
                        }}
                        selected={localData?.bodyColor}
                        t={t}
                        onBlur={props.onBlur}
                        placeholder={t("DEONAR_BODY_COLOR")}
                      />
                    )}
                  />
                </LabelFieldPair>
              </div>
            </div>
          </div>
          <div className="bmc-card-row">
            <Header>{t("Animal_Characterstics_Specific")}</Header>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_EYES")}</CardLabel>
                <Controller
                  control={control}
                  name="eyes"
                  render={(props) => (
                    <Dropdown
                      option={eyesOptions}
                      select={(value) => {
                        handleChange("eyes", value);
                        props.onChange(value);
                      }}
                      selected={localData?.eyes}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_EYES")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_NOSTRILS")}</CardLabel>
                <Controller
                  control={control}
                  name="nostrils"
                  render={(props) => (
                    <Dropdown
                      option={nostrilOptions}
                      select={(value) => {
                        handleChange("nostrils", value);
                        props.onChange(value);
                      }}
                      selected={localData?.nostrils}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_NOSTRILS")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_MUZZLE")}</CardLabel>
                <Controller
                  control={control}
                  name="muzzle"
                  render={(props) => (
                    <Dropdown
                      option={muzzleOptions}
                      select={(value) => {
                        handleChange("muzzle", value);
                        props.onChange(value);
                      }}
                      selected={localData?.muzzle}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_MUZZLE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_PREGNANCY")}</CardLabel>
                <Controller
                  control={control}
                  name="pregnancy"
                  render={(props) => (
                    <Dropdown
                      option={pregnancyOptions}
                      select={(value) => {
                        handleChange("pregnancy", value);
                        props.onChange(value);
                      }}
                      selected={localData?.pregnancy}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_PREGNANCY")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
          <div className="bmc-card-row">
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_BODY_TEMPERATURE")}</CardLabel>
                <Controller
                  control={control}
                  name="bodyTemp"
                  render={(props) => (
                    <Dropdown
                      option={bodyTempOptions}
                      select={(value) => {
                        handleChange("bodyTemp", value);
                        props.onChange(value);
                      }}
                      selected={localData?.bodyTemp}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_BODY_TEMPERATURE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_PULSE_RATE")}</CardLabel>
                <Controller
                  control={control}
                  name="pulseRate"
                  render={(props) => (
                    <Dropdown
                      option={pulseOptions}
                      select={(value) => {
                        handleChange("pulseRate", value);
                        props.onChange(value);
                      }}
                      selected={localData?.pulseRate}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_PULSE_RATE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_POSTURE")}</CardLabel>
                <Controller
                  control={control}
                  name="posture"
                  render={(props) => (
                    <Dropdown
                      option={postureOptions}
                      select={(value) => {
                        handleChange("posture", value);
                        props.onChange(value);
                      }}
                      selected={localData?.posture}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_POSTURE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_GAIT")}</CardLabel>
                <Controller
                  control={control}
                  name="gait"
                  render={(props) => (
                    <Dropdown
                      option={gaitOptions}
                      select={(value) => {
                        handleChange("gait", value);
                        props.onChange(value);
                      }}
                      selected={localData?.gait}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_GAIT")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
          <div className="bmc-card-row">
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_APPETITE")}</CardLabel>
                <Controller
                  control={control}
                  name="appetite"
                  render={(props) => (
                    <Dropdown
                      option={appetiteOptions}
                      select={(value) => {
                        handleChange("appetite", value);
                        props.onChange(value);
                      }}
                      selected={localData?.appetite}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_APPETITE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
          <div className="bmc-card-row">
            <Header>{t("Remarks")}</Header>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_OPINION")}</CardLabel>
                <Controller
                  control={control}
                  name="opinion"
                  render={(props) => (
                    <Dropdown
                      option={opinionOptions}
                      select={(value) => {
                        handleChange("opinion", value);
                        props.onChange(value);
                      }}
                      selected={localData?.opinion}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_OPINION")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_OTHER")}</CardLabel>
                <Controller
                  control={control}
                  name="other"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <TextInput
                        onChange={(e) => {
                          props.onChange(e.target.value);
                          handleChange("other", e.target.value);
                        }}
                        onBlur={props.onBlur}
                        optionKey="name"
                        t={t}
                        defaultValue={localData?.other || ""}
                        placeholder={t("DEONAR_OTHER")}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_REMARK")}</CardLabel>
                <Controller
                  control={control}
                  name="resultremark"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <TextInput
                        defaultValue={localData?.resultremark || ""}
                        onBlur={props.onBlur}
                        onChange={(e) => {
                          props.onChange(e.target.value);
                          handleChange("resultremark", e.target.value);
                        }}
                        optionKey="i18nKey"
                        t={t}
                        placeholder={t("DEONAR_REMARK")}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
        </React.Fragment>
        <div className="bmc-card-row" style={{ textAlign: "end", paddingBottom: "1rem" }}>
          <button type="button" className="bmc-card-button" style={{ borderBottom: "3px solid black", outline: "none" }} onClick={handleSave}>
            {t("Deonar_Update")}
          </button>
        </div>
      </form>
    </CustomModal>
  );
};

export const BeforeSlauhterInspectionModal = ({
  isModalOpen,
  toggleModal,
  control,
  inspectionTableData,
  slaughterReceiptNumber,
  speciesOptions,
  breedOptions,
  sexOptions,
  approxAgeOptions,
  bodyColorOptions,
  pregnancyOptions,
  opinionOptions,
  eyesOptions,
  nostrilOptions,
  muzzleOptions,
  bodyTempOptions,
  pulseOptions,
  postureOptions,
  gaitOptions,
  appetiteOptions,
  handleUpdateValue,
  selectedAnimal,
}) => {
  const { t } = useTranslation();
  const [localData, setLocalData] = useState(selectedAnimal);

  useEffect(() => {
    if (selectedAnimal) {
      setLocalData(selectedAnimal);
    }
  }, [selectedAnimal]);

  const handleChange = (field, value) => {
    setLocalData((prevData) => ({
      ...prevData,
      [field]: value,
    }));
  };

  const handleSave = () => {
    handleUpdateValue(localData);
    toggleModal();
  };
  if (!selectedAnimal) return null;

  return (
    <CustomModal isOpen={isModalOpen} onClose={toggleModal} fullScreen>
      <form>
        <React.Fragment>
          <div className="bmc-card-row">
            <div style={{ paddingBottom: "20px", display: "flex", gap: "12px", alignItems: "center" }}>
              <h3 style={{ fontWeight: "600", fontSize: "20px" }}>{t("Active Animal Token Number")}: </h3>
              <span
                style={{
                  fontWeight: "bold",
                  backgroundColor: "rgb(204, 204, 204)",
                  borderRadius: "10px",
                  padding: "8px",
                  fontSize: "22px",
                }}
              >
                {localData?.animal}
              </span>
            </div>
            <Header>{t("Animal_Characterstics_General")}</Header>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_SLAUGHTER_RECEIPT_NUMBER")}</CardLabel>
                <Controller
                  control={control}
                  name="slaughterReceiptNumber"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <TextInput
                        defaultValue={localData?.slaughterReceiptNumber || ""}
                        onBlur={props.onBlur}
                        onChange={(e) => {
                          props.onChange(e.target.value);
                          handleChange("slaughterReceiptNumber", e.target.value);
                        }}
                        optionKey="i18nKey"
                        t={t}
                        placeholder={t("DEONAR_SLAUGHTER_RECEIPT_NUMBER")}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_SPECIES")}</CardLabel>
                <Controller
                  control={control}
                  name="species"
                  render={(props) => (
                    <Dropdown
                      option={speciesOptions}
                      select={(value) => {
                        handleChange("species", value);
                        props.onChange(value);
                      }}
                      selected={localData?.species}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_SPECIES")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_BREED")}</CardLabel>
                <Controller
                  control={control}
                  name="breed"
                  render={(props) => (
                    <Dropdown
                      option={breedOptions}
                      select={(value) => {
                        handleChange("breed", value);
                        props.onChange(value);
                      }}
                      selected={localData?.breed}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_BREED")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_SEX")}</CardLabel>
                <Controller
                  control={control}
                  name="sex"
                  render={(props) => (
                    <Dropdown
                      option={sexOptions}
                      select={(value) => {
                        handleChange("sex", value);
                        props.onChange(value);
                      }}
                      selected={localData?.sex}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_SEX")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-card-row">
              <div className="bmc-col3-card">
                <LabelFieldPair>
                  <CardLabel className="bmc-label">{t("DEONAR_APPROXIMATE_AGE")}</CardLabel>
                  <Controller
                    control={control}
                    name="approxAge"
                    render={(props) => (
                      <Dropdown
                        option={approxAgeOptions}
                        select={(value) => {
                          handleChange("approxAge", value);
                          props.onChange(value);
                        }}
                        selected={localData?.approxAge}
                        t={t}
                        onBlur={props.onBlur}
                        placeholder={t("DEONAR_APPROXIMATE_AGE")}
                      />
                    )}
                  />
                </LabelFieldPair>
              </div>
              <div className="bmc-col3-card">
                <LabelFieldPair>
                  <CardLabel className="bmc-label">{t("DEONAR_BODY_COLOR")}</CardLabel>
                  <Controller
                    control={control}
                    name="bodyColor"
                    render={(props) => (
                      <Dropdown
                        option={bodyColorOptions}
                        select={(value) => {
                          handleChange("bodyColor", value);
                          props.onChange(value);
                        }}
                        selected={localData?.bodyColor}
                        t={t}
                        onBlur={props.onBlur}
                        placeholder={t("DEONAR_BODY_COLOR")}
                      />
                    )}
                  />
                </LabelFieldPair>
              </div>
            </div>
          </div>
          <div className="bmc-card-row">
            <Header>{t("Animal_Characterstics_Specific")}</Header>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_EYES")}</CardLabel>
                <Controller
                  control={control}
                  name="eyes"
                  render={(props) => (
                    <Dropdown
                      option={eyesOptions}
                      select={(value) => {
                        handleChange("eyes", value);
                        props.onChange(value);
                      }}
                      selected={localData?.eyes}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_EYES")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_NOSTRILS")}</CardLabel>
                <Controller
                  control={control}
                  name="nostrils"
                  render={(props) => (
                    <Dropdown
                      option={nostrilOptions}
                      select={(value) => {
                        handleChange("nostrils", value);
                        props.onChange(value);
                      }}
                      selected={localData?.nostrils}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_NOSTRILS")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_MUZZLE")}</CardLabel>
                <Controller
                  control={control}
                  name="muzzle"
                  render={(props) => (
                    <Dropdown
                      option={muzzleOptions}
                      select={(value) => {
                        handleChange("muzzle", value);
                        props.onChange(value);
                      }}
                      selected={localData?.muzzle}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_MUZZLE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_PREGNANCY")}</CardLabel>
                <Controller
                  control={control}
                  name="pregnancy"
                  render={(props) => (
                    <Dropdown
                      option={pregnancyOptions}
                      select={(value) => {
                        handleChange("pregnancy", value);
                        props.onChange(value);
                      }}
                      selected={localData?.pregnancy}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_PREGNANCY")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
          <div className="bmc-card-row">
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_BODY_TEMPERATURE")}</CardLabel>
                <Controller
                  control={control}
                  name="bodyTemp"
                  render={(props) => (
                    <Dropdown
                      option={bodyTempOptions}
                      select={(value) => {
                        handleChange("bodyTemp", value);
                        props.onChange(value);
                      }}
                      selected={localData?.bodyTemp}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_BODY_TEMPERATURE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_PULSE_RATE")}</CardLabel>
                <Controller
                  control={control}
                  name="pulseRate"
                  render={(props) => (
                    <Dropdown
                      option={pulseOptions}
                      select={(value) => {
                        handleChange("pulseRate", value);
                        props.onChange(value);
                      }}
                      selected={localData?.pulseRate}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_PULSE_RATE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_POSTURE")}</CardLabel>
                <Controller
                  control={control}
                  name="posture"
                  render={(props) => (
                    <Dropdown
                      option={postureOptions}
                      select={(value) => {
                        handleChange("posture", value);
                        props.onChange(value);
                      }}
                      selected={localData?.posture}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_POSTURE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_GAIT")}</CardLabel>
                <Controller
                  control={control}
                  name="gait"
                  render={(props) => (
                    <Dropdown
                      option={gaitOptions}
                      select={(value) => {
                        handleChange("gait", value);
                        props.onChange(value);
                      }}
                      selected={localData?.gait}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_GAIT")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
          <div className="bmc-card-row">
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_APPETITE")}</CardLabel>
                <Controller
                  control={control}
                  name="appetite"
                  render={(props) => (
                    <Dropdown
                      option={appetiteOptions}
                      select={(value) => {
                        handleChange("appetite", value);
                        props.onChange(value);
                      }}
                      selected={localData?.appetite}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_APPETITE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
          <div className="bmc-card-row">
            <Header>{t("Remarks")}</Header>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_OPINION")}</CardLabel>
                <Controller
                  control={control}
                  name="opinion"
                  render={(props) => (
                    <Dropdown
                      option={opinionOptions}
                      select={(value) => {
                        handleChange("opinion", value);
                        props.onChange(value);
                      }}
                      selected={localData?.opinion}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_OPINION")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_OTHER")}</CardLabel>
                <Controller
                  control={control}
                  name="other"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <TextInput
                        onChange={(e) => {
                          props.onChange(e.target.value);
                          handleChange("other", e.target.value);
                        }}
                        onBlur={props.onBlur}
                        optionKey="name"
                        t={t}
                        defaultValue={localData?.other || ""}
                        placeholder={t("DEONAR_OTHER")}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_REMARK")}</CardLabel>
                <Controller
                  control={control}
                  name="resultremark"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <TextInput
                        defaultValue={localData?.resultremark || ""}
                        onBlur={props.onBlur}
                        onChange={(e) => {
                          props.onChange(e.target.value);
                          handleChange("resultremark", e.target.value);
                        }}
                        optionKey="i18nKey"
                        t={t}
                        placeholder={t("DEONAR_REMARK")}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
          <div className="bmc-card-row" style={{ textAlign: "end", paddingBottom: "1rem" }}>
            <button type="button" className="bmc-card-button" style={{ borderBottom: "3px solid black", outline: "none" }} onClick={handleSave}>
              {t("Deonar_Update")}
            </button>
          </div>
        </React.Fragment>
      </form>
    </CustomModal>
  );
};

export const PostMortemInspectionModal = ({
  isModalOpen,
  toggleModal,
  control,
  inspectionTableData,
  slaughterReceiptNumber,
  speciesOptions,
  breedOptions,
  sexOptions,
  approxAgeOptions,
  bodyColorOptions,
  pregnancyOptions,
  opinionOptions,
  visibleMucusMembraneOptions,
  thoracicCavityOptions,
  abdominalCavityOptions,
  pelvicCavityOptions,
  specimenCollectionOptions,
  specialObservationOptions,
  handleUpdateValue,
  selectedAnimal,
  animalQuarters,
}) => {
  const { t } = useTranslation();
  const [localData, setLocalData] = useState(selectedAnimal);
  const [showAnimalQuaters, setShowAnimalQuarters] = useState(false);

  useEffect(() => {
    if (selectedAnimal) {
      setLocalData(selectedAnimal);
    }
  }, [selectedAnimal]);

  const handleChange = (field, value) => {
    setLocalData((prevData) => ({
      ...prevData,
      [field]: value,
    }));

    if (field === "opinion" && value === "Partially Condemnation" && localData?.animalType === "Buffalo") {
      setShowAnimalQuarters(true);
    }

    if (field === "animalTypeId" && value === "3") {
      setShowAnimalQuarters(true);
    }

    if (field === "opinion" && value !== "Partially Condemnation" && localData?.animalTypeId !== "3") {
      setShowAnimalQuarters(false);
    }
  };
  const handleSave = () => {
    if (localData?.opinion === "Passed" && localData.animalTypeId === 3 && localData?.animalType === "Buffalo") {
      localData.animalQuarters = 4;
    } else if (localData?.animalQuarters === "") {
      localData.animalQuarters = 1;
    }
    handleUpdateValue(localData);
    toggleModal();
  };

  if (!selectedAnimal) return null;
  return (
    <CustomModal isOpen={isModalOpen} onClose={toggleModal} fullScreen>
      <form>
        <React.Fragment>
          <div className="bmc-card-row">
            <div style={{ paddingBottom: "20px", display: "flex", gap: "12px", alignItems: "center" }}>
              <h3 style={{ fontWeight: "600", fontSize: "20px" }}>{t("Active Animal Token Number")}: </h3>
              <span
                style={{
                  fontWeight: "bold",
                  backgroundColor: "rgb(204, 204, 204)",
                  borderRadius: "10px",
                  padding: "8px",
                  fontSize: "22px",
                }}
              >
                {localData?.animal}
              </span>
            </div>
            <Header>{t("Animal_Characterstics_General")}</Header>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_SLAUGHTER_RECEIPT_NUMBER")}</CardLabel>
                <Controller
                  control={control}
                  name="slaughterReceiptNumber"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <TextInput
                        defaultValue={localData?.slaughterReceiptNumber || ""}
                        onBlur={props.onBlur}
                        onChange={(e) => {
                          props.onChange(e.target.value);
                          handleChange("slaughterReceiptNumber", e.target.value);
                        }}
                        optionKey="i18nKey"
                        t={t}
                        placeholder={t("DEONAR_SLAUGHTER_RECEIPT_NUMBER")}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_SPECIES")}</CardLabel>
                <Controller
                  control={control}
                  name="species"
                  render={(props) => (
                    <Dropdown
                      option={speciesOptions}
                      select={(value) => {
                        handleChange("species", value);
                        props.onChange(value);
                      }}
                      selected={localData?.species}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_SPECIES")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_BREED")}</CardLabel>
                <Controller
                  control={control}
                  name="breed"
                  render={(props) => (
                    <Dropdown
                      option={breedOptions}
                      select={(value) => {
                        handleChange("breed", value);
                        props.onChange(value);
                      }}
                      selected={localData?.breed}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_BREED")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_SEX")}</CardLabel>
                <Controller
                  control={control}
                  name="sex"
                  render={(props) => (
                    <Dropdown
                      option={sexOptions}
                      select={(value) => {
                        handleChange("sex", value);
                        props.onChange(value);
                      }}
                      selected={localData?.sex}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_SEX")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_APPROXIMATE_AGE")}</CardLabel>
                <Controller
                  control={control}
                  name="approxAge"
                  render={(props) => (
                    <Dropdown
                      option={approxAgeOptions}
                      select={(value) => {
                        handleChange("approxAge", value);
                        props.onChange(value);
                      }}
                      selected={localData?.approxAge}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_APPROXIMATE_AGE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_BODY_COLOR")}</CardLabel>
                <Controller
                  control={control}
                  name="bodyColor"
                  render={(props) => (
                    <Dropdown
                      option={bodyColorOptions}
                      select={(value) => {
                        handleChange("bodyColor", value);
                        props.onChange(value);
                      }}
                      selected={localData?.bodyColor}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_BODY_COLOR")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_PREGNANCY")}</CardLabel>
                <Controller
                  control={control}
                  name="pregnancy"
                  render={(props) => (
                    <Dropdown
                      option={pregnancyOptions}
                      select={(value) => {
                        handleChange("pregnancy", value);
                        props.onChange(value);
                      }}
                      selected={localData?.pregnancy}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_PREGNANCY")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
          <div className="bmc-card-row">
            <Header>{t("Animal_Characterstics_Specific")}</Header>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_VISIBLE_MUCUS_MEMBRANE")}&nbsp;</CardLabel>
                <Controller
                  control={control}
                  name="visibleMucusMembrane"
                  render={(props) => (
                    <Dropdown
                      option={visibleMucusMembraneOptions}
                      select={props.onChange}
                      selected={localData?.visibleMucusMembrane}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_VISIBLE_MUCUS_MEMBRANE")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_THORACIC_CAVITY")}&nbsp;</CardLabel>
                <Controller
                  control={control}
                  name="thoracicCavity"
                  render={(props) => (
                    <Dropdown
                      option={thoracicCavityOptions}
                      select={props.onChange}
                      selected={localData?.thoracicCavity}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_THORACIC_CAVITY")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_ABDOMINAL_CAVITY")}&nbsp;</CardLabel>
                <Controller
                  control={control}
                  name="abdominalCavity"
                  render={(props) => (
                    <Dropdown
                      option={abdominalCavityOptions}
                      select={props.onChange}
                      selected={localData?.abdominalCavity}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_ABDOMINAL_CAVITY")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_PELVIC_CAVITY")}&nbsp;</CardLabel>
                <Controller
                  control={control}
                  name="pelvicCavity"
                  render={(props) => (
                    <Dropdown
                      option={pelvicCavityOptions}
                      select={props.onChange}
                      selected={localData?.pelvicCavity}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_PELVIC_CAVITY")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
          <div className="bmc-card-row">
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_SPECIMEN_COLLECTION")}&nbsp;</CardLabel>
                <Controller
                  control={control}
                  name="specimenCollection"
                  render={(props) => (
                    <Dropdown
                      option={specimenCollectionOptions}
                      select={props.onChange}
                      selected={localData?.specimenCollection}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_SPECIMEN_COLLECTION")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_SPECIAL_OBSERVATION")}</CardLabel>
                <Controller
                  control={control}
                  name="specialObservation"
                  render={(props) => (
                    <Dropdown
                      option={specialObservationOptions}
                      select={props.onChange}
                      selected={localData?.specialObservation}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_SPECIAL_OBSERVATION")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>

          <div className="bmc-card-row">
            <Header>{t("Remarks")}</Header>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_OPINION")}</CardLabel>
                <Controller
                  control={control}
                  name="opinion"
                  render={(props) => (
                    <Dropdown
                      option={opinionOptions}
                      select={(value) => {
                        handleChange("opinion", value);
                        props.onChange(value);
                      }}
                      selected={localData?.opinion}
                      t={t}
                      onBlur={props.onBlur}
                      placeholder={t("DEONAR_OPINION")}
                    />
                  )}
                />
              </LabelFieldPair>
            </div>

            {showAnimalQuaters && (
              <div className="bmc-col3-card">
                <LabelFieldPair>
                  <CardLabel className="bmc-label">{t("DEONAR_ANIMAL_QUARTER")}</CardLabel>
                  <Controller
                    control={control}
                    name="animalQuarters"
                    render={(props) => (
                      <Dropdown
                        option={animalQuarters}
                        select={(value) => {
                          handleChange("animalQuarters", value);
                          props.onChange(value);
                        }}
                        selected={localData?.animalQuarters}
                        t={t}
                        onBlur={props.onBlur}
                        placeholder={t("DEONAR_ANIMAL_QUARTER")}
                      />
                    )}
                  />
                </LabelFieldPair>
              </div>
            )}

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_OTHER")}</CardLabel>
                <Controller
                  control={control}
                  name="other"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <TextInput
                        onChange={(e) => {
                          props.onChange(e.target.value);
                          handleChange("other", e.target.value);
                        }}
                        onBlur={props.onBlur}
                        optionKey="name"
                        t={t}
                        defaultValue={localData?.other || ""}
                        placeholder={t("DEONAR_OTHER")}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>

            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_REMARK")}</CardLabel>
                <Controller
                  control={control}
                  name="resultremark"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <TextInput
                        defaultValue={localData?.resultremark || ""}
                        onBlur={props.onBlur}
                        onChange={(e) => {
                          props.onChange(e.target.value);
                          handleChange("resultremark", e.target.value);
                        }}
                        optionKey="i18nKey"
                        t={t}
                        placeholder={t("DEONAR_REMARK")}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>

          <div className="bmc-card-row" style={{ textAlign: "end", paddingBottom: "1rem" }}>
            <button type="button" className="bmc-card-button" style={{ borderBottom: "3px solid black", outline: "none" }} onClick={handleSave}>
              {t("Deonar_Update")}
            </button>
          </div>
        </React.Fragment>
      </form>
    </CustomModal>
  );
};
