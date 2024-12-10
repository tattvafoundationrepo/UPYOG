// import { CardLabel, Dropdown, LabelFieldPair } from "@upyog/digit-ui-react-components";
// import React, { useEffect, useState } from "react";
// import { Controller, useForm } from "react-hook-form";
// import { useTranslation } from "react-i18next";

// const SearchApplications = ({ onUpdate, actions }) => {
//   const { t } = useTranslation();
//   const {
//     control,
//     trigger,
//     clearErrors,
//     formState: { errors, isValid },
//   } = useForm({
//     defaultValues: {
//       schemeHead: "",
//       scheme: "",
//       details: "",
//     },
//     mode: "onChange",
//   });
//   const [schemeHeads, setSchemeHeads] = useState([]);
//   const [schemes, setSchemes] = useState([]);
//   const [details, setDetails] = useState([]);
//   const [selectedSchemeHead, setSelectedSchemeHead] = useState("");
//   const [selectedScheme, setSelectedScheme] = useState("");
//   const [selectedDetail, setSelectedDetail] = useState("");
//   const [selectedType, setSelectedType] = useState("");
//   const getSchemes = { SchemeSearchCriteria: { Status: 1, sla: 30, action: actions } };
//   const { data: SCHEME_DATA } = Digit.Hooks.bmc.useSchemesGet(getSchemes);

//   useEffect(() => {
//     if (SCHEME_DATA && SCHEME_DATA.SchemeDetails) {
//       const groupedSchemeHeads = [];
//       const schemeHeadMap = new Map();

//       SCHEME_DATA.SchemeDetails.forEach((event) => {
//         event.schemeshead.forEach((schemeHead) => {
//           if (!schemeHeadMap.has(schemeHead.schemeHead)) {
//             schemeHeadMap.set(schemeHead.schemeHead, {
//               schemeHead: schemeHead.schemeHead,
//               schemeheadDesc: schemeHead.schemeheadDesc,
//               schemeHeadApplicationCount: schemeHead?.schemeHeadApplicationCount || 0,
//               schemeDetails: [...schemeHead.schemeDetails],
//             });
//           } else {
//             const existingSchemeHead = schemeHeadMap.get(schemeHead.schemeHead);
//             existingSchemeHead.schemeDetails.push(...schemeHead.schemeDetails);
//           }
//         });
//       });

//       schemeHeadMap.forEach((value) => groupedSchemeHeads.push(value));
//       setSchemeHeads(groupedSchemeHeads);
//     }
//   }, [SCHEME_DATA]);

//   const handleSchemeHeadChange = (selected) => {
//     setSelectedSchemeHead(selected);
//     setSelectedScheme("");
//     setSelectedDetail("");
//     setSelectedType("");
//     setDetails([]);
//     clearErrors("schemeHead");
//     const selectedSchemeDetails = schemeHeads.find((head) => head.schemeHead === selected.value)?.schemeDetails || [];
//     setSchemes(selectedSchemeDetails);
//   };

//   const handleSchemeChange = (selected) => {
//     setSelectedScheme(selected.value);
//     setSelectedDetail("");
//     setSelectedType("");

//     const selectedScheme = schemes.find((scheme) => scheme.schemeID === selected.value);
//     const details = [...(selectedScheme.courses || []), ...(selectedScheme.machines || [])];
//     setDetails(details);
//     clearErrors("scheme");
//   };

//   const handleDetailChange = (selected) => {
//     setSelectedDetail(selected.value);
//     setSelectedType(selected.type);
//   };

//   const schemeHeadOptions = schemeHeads.map((head) => ({
//     value: head.schemeHead,
//     label: `${t(head.schemeHead)} (${head.schemeHeadApplicationCount || 0})`,
//   }));

//   const schemeOptions = schemes.map((scheme) => ({
//     value: scheme.schemeID,
//     label: `${t(scheme.schemeName)} (${scheme.schemeApplicationCount || 0})`,
//   }));

//   const detailOptions = details.map((detail) => ({
//     value: detail.machID || detail.courseID,
//     label: `${t(detail.machName || detail.courseName)} (${
//       detail.machID ? detail.machineWiseApplicationCount || 0 : detail.courseWiseApplicationCount || 0
//     })`,
//     type: detail.machID ? "machine" : "course",
//   }));

//   const handleSearch = () => {
//     let searchCriteria = {};

//     if (selectedSchemeHead) {
//       searchCriteria.schemeHead = selectedSchemeHead;
//     }
//     if (selectedScheme) {
//       searchCriteria.schemeID = selectedScheme;
//     }
//     if (selectedDetail) {
//       searchCriteria.detailID = selectedDetail;
//     }

//     if (selectedType) {
//       searchCriteria.type = selectedType;
//     }
//     if (onUpdate) {
//       onUpdate(searchCriteria);
//     }
//   };

//   useEffect(() => {
//     trigger(); // Validate the form on mount to show errors if fields are empty
//   }, [trigger]);

//   return (
//     <React.Fragment>
//       <div className="bmc-row-card-header">
//         <div className="bmc-card-row">
//           <div className="bmc-col3-card">
//             <LabelFieldPair>
//               <CardLabel className="bmc-label">
//                 {t("Scheme Heads")}&nbsp;{errors.schemeHead && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.schemeHead.message}</sup>}
//               </CardLabel>
//               <Controller
//                 control={control}
//                 name="schemeHead"
//                 rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
//                 render={({ value, onChange, onBlur }) => (
//                   <div>
//                     <Dropdown
//                       placeholder={t("Select Scheme Head")}
//                       selected={value}
//                       defaultValue={""}
//                       select={(selectedValue) => {
//                         const isValid = schemeHeadOptions.some((option) => option.value === selectedValue.value);

//                         if (isValid) {
//                           onChange(selectedValue);
//                           handleSchemeHeadChange(selectedValue);
//                         } else {
//                           onChange("");
//                           setSelectedScheme("");
//                           setSelectedDetail("");
//                           setDetails([]);
//                           clearErrors("schemeHead");
//                         }
//                       }}
//                       onBlur={onBlur}
//                       option={schemeHeadOptions}
//                       optionKey="label"
//                       t={t}
//                     />
//                   </div>
//                 )}
//               />
//             </LabelFieldPair>
//           </div>
//           <div className="bmc-col3-card">
//             <LabelFieldPair>
//               <CardLabel className="bmc-label">
//                 {t("Schemes")}&nbsp;{errors.scheme && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.scheme.message}</sup>}
//               </CardLabel>
//               <Controller
//                 control={control}
//                 name="scheme"
//                 rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
//                 render={({ value, onChange, onBlur }) => (
//                   <div>
//                     <Dropdown
//                       placeholder={t("Select Scheme")}
//                       selected={schemeOptions.find((option) => option.value === value)}
//                       select={(option) => {
//                         onChange(option.value);
//                         handleSchemeChange(option);
//                       }}
//                       onBlur={onBlur}
//                       option={schemeOptions}
//                       optionKey="label"
//                       t={t}
//                       disabled={!selectedSchemeHead}
//                     />
//                   </div>
//                 )}
//               />
//             </LabelFieldPair>
//           </div>
//           <div className="bmc-col3-card">
//             <LabelFieldPair>
//               <CardLabel className="bmc-label">{t("Details")}</CardLabel>
//               <Controller
//                 control={control}
//                 name="details"
//                 render={({ value, onChange, onBlur }) => (
//                   <div>
//                     <Dropdown
//                       placeholder={t("Select Details")}
//                       selected={detailOptions.find((option) => option.value === value)}
//                       select={(option) => {
//                         onChange(option.value);
//                         handleDetailChange(option);
//                       }}
//                       onBlur={onBlur}
//                       option={detailOptions}
//                       optionKey="label"
//                       t={t}
//                       disabled={!selectedScheme}
//                     />
//                   </div>
//                 )}
//               />
//             </LabelFieldPair>
//           </div>
//           <div className="bmc-col3-card">
//             <div className="bmc-search-button" style={{ textAlign: "end", paddingTop: "21px" }}>
//               <button
//                 className="bmc-card-button"
//                 onClick={handleSearch}
//                 style={{ borderBottom: "3px solid black" }}
//                 disabled={!isValid || Object.keys(errors).length > 0}
//               >
//                 {t("Search")}
//               </button>
//             </div>
//           </div>
//         </div>
//       </div>
//     </React.Fragment>
//   );
// };

// export default SearchApplications;

import { CardLabel, Dropdown, LabelFieldPair } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";

const SearchApplications = ({ onUpdate, actions }) => {
  const { t } = useTranslation();
  const {
    control,
    trigger,
    clearErrors,
    formState: { errors, isValid },
    getValues,
  } = useForm({
    defaultValues: {
      schemeHead: "",
      scheme: "",
      details: "",
    },
    mode: "onChange",
  });
  const [schemeHeads, setSchemeHeads] = useState([]);
  const [schemes, setSchemes] = useState([]);
  const [details, setDetails] = useState([]);
  const [selectedSchemeHead, setSelectedSchemeHead] = useState("");
  const [selectedScheme, setSelectedScheme] = useState("");
  const [selectedDetail, setSelectedDetail] = useState("");
  const [selectedType, setSelectedType] = useState("");

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
    setSelectedSchemeHead(selected.value);
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

    if (selectedType) {
      searchCriteria.type = selectedType;
    }
    if (onUpdate) {
      onUpdate(searchCriteria);
    }
  };

  const isSearchButtonEnabled = () => {
    return selectedSchemeHead && selectedScheme;
  };

  useEffect(() => {
    trigger(); // Validate the form on mount to show errors if fields are empty
  }, [trigger]);

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
                      select={(selectedValue) => {
                        const isValid = schemeHeadOptions.some((option) => option.value === selectedValue.value);

                        if (isValid) {
                          onChange(selectedValue);
                          handleSchemeHeadChange(selectedValue);
                        } else {
                          onChange("");
                          setSelectedScheme("");
                          setSelectedDetail("");
                          setDetails([]);
                          clearErrors("schemeHead");
                        }
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
                      disabled={!selectedSchemeHead} // Disabled until Scheme Head is selected
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
                      disabled={!selectedScheme} // Disabled until Scheme is selected
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <div className="bmc-search-button" style={{ textAlign: "end", paddingTop: "21px" }}>
              <button
                className="bmc-card-button"
                onClick={handleSearch}
                style={{ borderBottom: "3px solid black", backgroundColor: isSearchButtonEnabled() ? "#f47738" : "grey" }}
                disabled={!isSearchButtonEnabled() || Object.keys(errors).length > 0}
              >
                {t("Search")}
              </button>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default SearchApplications;
