import { CardLabel, Dropdown, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { Modal } from "@upyog/digit-ui-react-components";

const RandomizeApplications = ({ onUpdate, actions }) => {
  const { t } = useTranslation();
  const {
    control,
    trigger,
    clearErrors,
    formState: { errors, isValid },
  } = useForm({
    defaultValues: {
      schemeHead: "",
      scheme: "",
      details: "",
      machineNumber: "",
    },
    mode: "onChange",
  });
  const [schemeHeads, setSchemeHeads] = useState([]);
  const [schemes, setSchemes] = useState([]);
  const [details, setDetails] = useState([]);
  const availableActions = ["RANDOMIZE"];
  const [selectedAction, setSelectedAction] = useState(availableActions[0]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedSchemeHead, setSelectedSchemeHead] = useState("");
  const [selectedScheme, setSelectedScheme] = useState("");
  const [selectedDetail, setSelectedDetail] = useState("");
  const [selectedType, setSelectedType] = useState("");
  const [machineNumber, setMachineNumber] = useState("");
  const getSchemes = { SchemeSearchCriteria: { Status: 1, sla: 30, action: actions } };
  const { data: SCHEME_DATA } = Digit.Hooks.bmc.useSchemesGet(getSchemes);

  useEffect(() => {
    if (SCHEME_DATA && SCHEME_DATA.SchemeDetails) {
      const groupedSchemeHeads = [];
      const schemeHeadMap = new Map();

      SCHEME_DATA.SchemeDetails.forEach((event) => {
        event.schemeshead.forEach((schemeHead) => {
          if (!schemeHeadMap.has(schemeHead.schemeHead)) {
            schemeHeadMap.set(schemeHead.schemeHead, {
              schemeHead: schemeHead.schemeHead,
              schemeheadDesc: schemeHead.schemeheadDesc,
              schemeHeadApplicationCount: schemeHead?.schemeHeadApplicationCount || 0,
              schemeDetails: [...schemeHead.schemeDetails],
            });
          } else {
            const existingSchemeHead = schemeHeadMap.get(schemeHead.schemeHead);
            existingSchemeHead.schemeDetails.push(...schemeHead.schemeDetails);
          }
        });
      });

      schemeHeadMap.forEach((value) => groupedSchemeHeads.push(value));
      setSchemeHeads(groupedSchemeHeads);
    }
  }, [SCHEME_DATA]);

  const handleSchemeHeadChange = (selected) => {
    setSelectedSchemeHead(selected);
    setSelectedScheme("");
    setSelectedDetail("");
    setSelectedType("");
    setDetails([]);
    clearErrors("schemeHead");
    const selectedSchemeDetails = schemeHeads.find((head) => head.schemeHead === selected.value)?.schemeDetails || [];
    setSchemes(selectedSchemeDetails);
  };

  const handleSchemeChange = (selected) => {
    setSelectedScheme(selected.value);
    setSelectedDetail("");
    setSelectedType("");

    const selectedScheme = schemes.find((scheme) => scheme.schemeID === selected.value);
    const details = [...(selectedScheme.courses || []), ...(selectedScheme.machines || [])];
    setDetails(details);
    clearErrors("scheme");
  };

  const handleDetailChange = (selected) => {
    setSelectedDetail(selected.value);
    setSelectedType(selected.type);
  };

  const schemeHeadOptions = schemeHeads.map((head) => ({
    value: head.schemeHead,
    label: `${t(head.schemeHead)} (${head.schemeHeadApplicationCount || 0})`,
  }));

  const schemeOptions = schemes.map((scheme) => ({
    value: scheme.schemeID,
    label: `${t(scheme.schemeName)} (${scheme.schemeApplicationCount || 0})`,
  }));

  const detailOptions = details.map((detail) => ({
    value: detail.machID || detail.courseID,
    label: `${t(detail.machName || detail.courseName)} (${
      detail.machID ? detail.machineWiseApplicationCount || 0 : detail.courseWiseApplicationCount || 0
    })`,
    type: detail.machID ? "machine" : "course",
  }));

  const handleSearch = () => {
    let searchCriteria = {};

    if (selectedSchemeHead) {
      searchCriteria.schemeHead = selectedSchemeHead;
    }
    if (selectedScheme) {
      searchCriteria.schemeID = selectedScheme;
    }
    if (selectedDetail) {
      searchCriteria.detailID = selectedDetail;
    }

    if (machineNumber) {
      searchCriteria.machineNumber = machineNumber;
    }

    if (selectedType) {
      searchCriteria.type = selectedType;
    }
    if (onUpdate) {
      onUpdate(searchCriteria);
    }
    setIsModalOpen(false);
  };

  const openModal = () => {
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
  };

  const isSearchButtonEnabled = () => {
    return selectedSchemeHead && selectedScheme;
  };

  useEffect(() => {
    if (machineNumber >= 1) {
      clearErrors("machineNumber"); // Clears all errors when machineNumber is greater than 1
    } else {
      trigger(); // Re-trigger validation when machineNumber is 1 or less
    }
  }, [machineNumber, clearErrors, trigger]);

  const Close = () => (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF">
      <path d="M0 0h24v24H0V0z" fill="none" />
      <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
    </svg>
  );

  const CloseBtn = (props) => {
    return (
      <div className="icon-bg-secondary" onClick={props.onClick}>
        <Close />
      </div>
    );
  };
  //onClick={handleSearch}
  const handleRandomAll = () => {
    // const data = {
    //   ApplicationStatus: {
    //     action: "SELECTED",
    //     Comment: Comment || null,
    //     ApplicationNumbers: selectedRows || [],
    //   },
    // };
    // getVerifyScheme.mutate(data, {
    //   onSuccess: () => {
    //     setApplications((prevApplications) => prevApplications.filter((application) => !selectedRows.includes(application.applicationNumber)));
    //     setSelectedRows([]);
    //     setIsModalOpen(false);
    //   },
    //   onError: (err) => {
    //     console.error("Error verifying applications:", err);
    //   },
    // });
  };

  return (
    <React.Fragment>
      <div className="bmc-row-card-header">
        <div className="bmc-card-row">
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("Scheme Heads")}&nbsp;{errors.schemeHead && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.schemeHead.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="schemeHead"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={({ value, onChange, onBlur }) => (
                  <div>
                    <Dropdown
                      placeholder={t("Select Scheme Head")}
                      selected={value}
                      defaultValue={""}
                      select={(value) => {
                        onChange(value);
                        handleSchemeHeadChange(value);
                      }}
                      onBlur={onBlur}
                      option={schemeHeadOptions}
                      optionKey="label"
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
                {t("Schemes")}&nbsp;{errors.scheme && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.scheme.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="scheme"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={({ value, onChange, onBlur }) => (
                  <div>
                    <Dropdown
                      placeholder={t("Select Scheme")}
                      selected={schemeOptions.find((option) => option.value === value)}
                      select={(option) => {
                        onChange(option.value);
                        handleSchemeChange(option);
                      }}
                      onBlur={onBlur}
                      option={schemeOptions}
                      optionKey="label"
                      t={t}
                      disabled={!selectedSchemeHead}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">{t("Details")}</CardLabel>
              <Controller
                control={control}
                name="details"
                render={({ value, onChange, onBlur }) => (
                  <div>
                    <Dropdown
                      placeholder={t("Select Details")}
                      selected={detailOptions.find((option) => option.value === value)}
                      select={(option) => {
                        onChange(option.value);
                        handleDetailChange(option);
                      }}
                      onBlur={onBlur}
                      option={detailOptions}
                      optionKey="label"
                      t={t}
                      disabled={!selectedScheme}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_RANDOMIZE_APPLICATION")}&nbsp;
                {errors.machineNumber && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.machineNumber.message}</sup>}
              </CardLabel>{" "}
              <Controller
                control={control}
                name="machineNumber"
                rules={{
                  required: t("CORE_COMMON_REQUIRED_ERRMSG"),
                  min: {
                    value: 1,
                    message: t("MUST BE AT LEAST 1"),
                  },
                }}
                defaultValue={machineNumber}
                render={(props) => (
                  <div>
                    <TextInput
                      placeholder={t("Number")}
                      value={props.value}
                      onChange={(e) => {
                        const value = e.target.value;
                        if (/^\d*$/.test(value)) {
                          setMachineNumber(value);
                          props.onChange(value);
                        }
                      }}
                      onBlur={props.onBlur}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
        </div>
        <div className="bmc-card-row" style={{ textAlign: "end", paddingBottom: "1rem", paddingRight: "1rem" }}>
          <div>
            <div className="bmc-search-button" style={{ textAlign: "end" }}>
              <button
                className="bmc-card-button"
                onClick={openModal}
                style={{ borderBottom: "3px solid black", backgroundColor: isSearchButtonEnabled() ? "#f47738" : "grey" }}
                disabled={!isSearchButtonEnabled() || !isValid || Object.keys(errors).length > 0}
              >
                {t("BMC_RANDOMIZE_SELECT")}
              </button>
            </div>
          </div>
          {isModalOpen && (
            <div className="bmc-modal">
              <Modal onClose={closeModal} fullScreen hideSubmit={true} headerBarEnd={<CloseBtn onClick={closeModal} />}>
                <p style={{ fontSize: "15px" }}>
                  <strong>
                    {machineNumber} {t("Applications will randomly get selected for each ward after this action!. Do you want to continue?")}
                  </strong>
                </p>
                <button
                  style={{
                    backgroundColor: "#F47738",
                    width: "91px",
                    height: "34px",
                    color: "white",
                    marginTop: "1.5rem",
                    borderBottom: "3px solid black",
                    outline: "none",
                    margin: "8px",
                  }}
                  onClick={handleSearch}
                >
                  {t("Submit")}
                </button>
                <button
                  style={{
                    backgroundColor: "grey",
                    width: "91px",
                    height: "34px",
                    color: "white",
                    marginTop: "1.5rem",
                    borderBottom: "3px solid black",
                    outline: "none",
                  }}
                  onClick={closeModal}
                >
                  {t("Cancel")}
                </button>
              </Modal>
            </div>
          )}
        </div>
      </div>
    </React.Fragment>
  );
};

export default RandomizeApplications;
