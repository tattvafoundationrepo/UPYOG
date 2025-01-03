import React, { useState, useEffect } from "react";
import { Controller } from "react-hook-form";
import { Header, LabelFieldPair, CardLabel, Dropdown, TextInput } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import CustomModal from "../commonFormFields/customModal";
import OpinionDropdown from "./opinionDropdown";

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
  const [otherSelected, setOtherSelected] = useState({});

  useEffect(() => {
    if (selectedAnimal) {
      setLocalData(selectedAnimal);
    }
  }, [selectedAnimal]);

  const handleSave = () => {
    handleUpdateValue(localData);
    toggleModal();
  };

  const handleChange = (field, value) => {
    setLocalData((prevData) => ({
      ...prevData,
      [field]: value,
    }));
  };

  const handleInputChange = (field, e) => {
    const value = e.target.value;
    setLocalData((prevData) => ({ ...prevData, [field]: value }));
    if (!value) {
      setOtherSelected((prev) => ({ ...prev, [field]: false }));
    }
  };

  const handleSelectChange = (field, selectedName) => {
    if (selectedName === "Other") {
      setOtherSelected((prev) => ({ ...prev, [field]: true }));
      setLocalData((prevData) => ({ ...prevData, [field]: "" }));
    } else {
      setOtherSelected((prev) => ({ ...prev, [field]: false }));
      setLocalData((prevData) => ({ ...prevData, [field]: selectedName }));
    }
  };

  const renderField = (field, label, options, placeholder) => (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t(label)}</CardLabel>
        <Controller
          control={control}
          name={field}
          render={(props) =>
            otherSelected[field] ? (
              <TextInput
                defaultValue={localData?.[field] || ""}
                onBlur={props.onBlur}
                onChange={(e) => {
                  handleInputChange(field, e);
                  props.onChange(e.target.value);
                }}
                t={t}
                placeholder={t(placeholder)}
              />
            ) : (
              <Dropdown
                option={options}
                select={(selectedName) => {
                  handleSelectChange(field, selectedName);
                  props.onChange(selectedName);
                }}
                selected={localData?.[field]}
                t={t}
                onBlur={props.onBlur}
                placeholder={t(placeholder)}
              />
            )
          }
        />
      </LabelFieldPair>
    </div>
  );

  if (!selectedAnimal) return null;

  return (
    <CustomModal isOpen={isModalOpen} onClose={toggleModal} fullScreen title={`${t("Active Animal Token Number")} - ${localData?.animal}`}>
      <form>
        <React.Fragment>
          <div className="bmc-card-row">
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

            {renderField("breed", "DEONAR_BREED", breedOptions, "DEONAR_BREED")}

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
            {renderField("approxAge", "DEONAR_APPROXIMATE_AGE", approxAgeOptions, "DEONAR_APPROXIMATE_AGE")}

            <div className="bmc-card-row">{renderField("bodyColor", "DEONAR_BODY_COLOR", bodyColorOptions, "DEONAR_BODY_COLOR")}</div>
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
            {renderField("nostrils", "DEONAR_NOSTRILS", nostrilOptions, "DEONAR_NOSTRILS")}
            {renderField("muzzle", "DEONAR_MUZZLE", muzzleOptions, "DEONAR_MUZZLE")}

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
            {renderField("bodyTemp", "DEONAR_BODY_TEMPERATURE", bodyTempOptions, "DEONAR_BODY_TEMPERATURE")}
            {renderField("pulseRate", "DEONAR_PULSE_RATE", pulseOptions, "DEONAR_PULSE_RATE")}
            {renderField("posture", "DEONAR_POSTURE", postureOptions, "DEONAR_POSTURE")}
            {renderField("gait", "DEONAR_GAIT", gaitOptions, "DEONAR_GAIT")}
          </div>

          <div className="bmc-card-row">{renderField("appetite", "DEONAR_APPETITE", appetiteOptions, "DEONAR_APPETITE")}</div>
          <div className="bmc-card-row">
            <Header>{t("Remarks")}</Header>
            <OpinionDropdown control={control} t={t} localData={localData} handleChange={handleChange} opinionOptions={opinionOptions} />
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
  const [otherSelected, setOtherSelected] = useState({});

  useEffect(() => {
    if (selectedAnimal) {
      setLocalData(selectedAnimal);
    }
  }, [selectedAnimal]);

  const handleInputChange = (field, e) => {
    const value = e.target.value;
    setLocalData((prevData) => ({ ...prevData, [field]: value }));
    if (!value) {
      setOtherSelected((prev) => ({ ...prev, [field]: false }));
    }
  };

  const handleSelectChange = (field, selectedName) => {
    if (selectedName === "Other") {
      setOtherSelected((prev) => ({ ...prev, [field]: true }));
      setLocalData((prevData) => ({ ...prevData, [field]: "" }));
    } else {
      setOtherSelected((prev) => ({ ...prev, [field]: false }));
      setLocalData((prevData) => ({ ...prevData, [field]: selectedName }));
    }
  };

  const renderField = (field, label, options, placeholder) => (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t(label)}</CardLabel>
        <Controller
          control={control}
          name={field}
          render={(props) =>
            otherSelected[field] ? (
              <TextInput
                defaultValue={localData?.[field] || ""}
                onBlur={props.onBlur}
                onChange={(e) => {
                  handleInputChange(field, e);
                  props.onChange(e.target.value);
                }}
                t={t}
                placeholder={t(placeholder)}
              />
            ) : (
              <Dropdown
                option={options}
                select={(selectedName) => {
                  handleSelectChange(field, selectedName);
                  props.onChange(selectedName);
                }}
                selected={localData?.[field]}
                t={t}
                onBlur={props.onBlur}
                placeholder={t(placeholder)}
              />
            )
          }
        />
      </LabelFieldPair>
    </div>
  );

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
    <CustomModal isOpen={isModalOpen} onClose={toggleModal} fullScreen title={`${t("Active Animal Token Number")} - ${localData?.animal}`}>
      <form>
        <React.Fragment>
          <div className="bmc-card-row">
            <Header>{t("Animal_Characterstics_General")}</Header>
            {/* <div className="bmc-col3-card">
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
            </div> */}

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

            {renderField("breed", "DEONAR_BREED", breedOptions, "DEONAR_BREED")}

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
            {renderField("approxAge", "DEONAR_APPROXIMATE_AGE", approxAgeOptions, "DEONAR_APPROXIMATE_AGE")}

            <div className="bmc-card-row">{renderField("bodyColor", "DEONAR_BODY_COLOR", bodyColorOptions, "DEONAR_BODY_COLOR")}</div>
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
            {renderField("nostrils", "DEONAR_NOSTRILS", nostrilOptions, "DEONAR_NOSTRILS")}
            {renderField("muzzle", "DEONAR_MUZZLE", muzzleOptions, "DEONAR_MUZZLE")}

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
            {renderField("bodyTemp", "DEONAR_BODY_TEMPERATURE", bodyTempOptions, "DEONAR_BODY_TEMPERATURE")}
            {renderField("pulseRate", "DEONAR_PULSE_RATE", pulseOptions, "DEONAR_PULSE_RATE")}
            {renderField("posture", "DEONAR_POSTURE", postureOptions, "DEONAR_POSTURE")}
            {renderField("gait", "DEONAR_GAIT", gaitOptions, "DEONAR_GAIT")}
          </div>

          <div className="bmc-card-row">{renderField("appetite", "DEONAR_APPETITE", appetiteOptions, "DEONAR_APPETITE")}</div>

          <div className="bmc-card-row">
            <Header>{t("Remarks")}</Header>
            <OpinionDropdown control={control} t={t} localData={localData} handleChange={handleChange} opinionOptions={opinionOptions} />

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
  const [otherSelected, setOtherSelected] = useState({});

  useEffect(() => {
    if (selectedAnimal) {
      setLocalData(selectedAnimal);
    }
  }, [selectedAnimal]);

  const handleInputChange = (field, e) => {
    const value = e.target.value;
    setLocalData((prevData) => ({ ...prevData, [field]: value }));
    if (!value) {
      setOtherSelected((prev) => ({ ...prev, [field]: false }));
    }
  };

  const handleSelectChange = (field, selectedName) => {
    if (selectedName === "Other") {
      setOtherSelected((prev) => ({ ...prev, [field]: true }));
      setLocalData((prevData) => ({ ...prevData, [field]: "" }));
    } else {
      setOtherSelected((prev) => ({ ...prev, [field]: false }));
      setLocalData((prevData) => ({ ...prevData, [field]: selectedName }));
    }
  };

  const renderField = (field, label, options, placeholder) => (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t(label)}</CardLabel>
        <Controller
          control={control}
          name={field}
          render={(props) =>
            otherSelected[field] ? (
              <TextInput
                defaultValue={localData?.[field] || ""}
                onBlur={props.onBlur}
                onChange={(e) => {
                  handleInputChange(field, e);
                  props.onChange(e.target.value);
                }}
                t={t}
                placeholder={t(placeholder)}
              />
            ) : (
              <Dropdown
                option={options}
                select={(selectedName) => {
                  handleSelectChange(field, selectedName);
                  props.onChange(selectedName);
                }}
                selected={localData?.[field]}
                t={t}
                onBlur={props.onBlur}
                placeholder={t(placeholder)}
              />
            )
          }
        />
      </LabelFieldPair>
    </div>
  );

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
    <CustomModal isOpen={isModalOpen} onClose={toggleModal} fullScreen title={`${t("Active Animal Token Number")} - ${localData?.animal}`}>
      <form>
        <React.Fragment>
          <div className="bmc-card-row">
            <Header>{t("Animal_Characterstics_General")}</Header>
            <div className="bmc-card-row">
              {/* <div className="bmc-col3-card">
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
            </div> */}

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

              {renderField("breed", "DEONAR_BREED", breedOptions, "DEONAR_BREED")}

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

              {renderField("approxAge", "DEONAR_APPROXIMATE_AGE", approxAgeOptions, "DEONAR_APPROXIMATE_AGE")}
            </div>
            <div className="bmc-card-row">
              {renderField("bodyColor", "DEONAR_BODY_COLOR", bodyColorOptions, "DEONAR_BODY_COLOR")}

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
            <OpinionDropdown control={control} t={t} localData={localData} handleChange={handleChange} opinionOptions={opinionOptions} />

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
