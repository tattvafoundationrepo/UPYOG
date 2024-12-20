import { ActionBar, ApplyFilterBar, CloseSvg, Dropdown, RadioButtons, RemoveableTag, SubmitBar } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import useBMCCommon from "@upyog/digit-ui-libraries/src/hooks/bmc/useBMCCommon";


const Filter = ({ searchParams, onFilterChange, onSearch, removeParam, ...props }) => {
  const { t } = useTranslation()
  const {
    control,
    trigger,
    clearErrors,
    formState: { isValid },
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

  const getSchemes = { SchemeSearchCriteria: { Status: 1, sla: 30 } };
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


  const schemeHeadOptions = schemeHeads.map((head) => ({
    value: head.schemeHead,
    label: `${(head.schemeHead)} `,
  }));

  const schemeOptions = schemes.map((scheme) => ({
    value: scheme.schemeID,
    label: `${(scheme.schemeName)} `,
  }));

  const detailOptions = details.map((detail) => ({
    value: detail.machID || detail.courseID,
    label: `${t(detail.machName || detail.courseName)} (${detail.machID ? detail.machineWiseApplicationCount || 0 : detail.courseWiseApplicationCount || 0
      })`,
    type: detail.machID ? "machine" : "course",
  }));


  useEffect(() => {
    trigger(); // Validate the form on mount to show errors if fields are empty
  }, [trigger]);

  const [filters, onSelectFilterRoles] = useState(searchParams?.filters?.role || { role: [] });
  const [_searchParams, setSearchParams] = useState(() => searchParams);
  const [selectedRoles, onSelectFilterRolessetSelectedRole] = useState(null);
  const tenantIds = Digit.SessionStorage.get("BMC_TENANTS");

  function onSelectRoles(value, type) {
    if (!ifExists(filters.role, value)) {
      onSelectFilterRoles({ ...filters, role: [...filters.role, value] });
    }
  }

  const onRemove = (index, key) => {
    let afterRemove = filters[key].filter((value, i) => {
      return i !== index;
    });
    onSelectFilterRoles({ ...filters, [key]: afterRemove });
  };

  useEffect(() => {
    if (filters.role.length > 1) {
      onSelectFilterRolessetSelectedRole({ name: `${filters.role.length} selected` });
    } else {
      onSelectFilterRolessetSelectedRole(filters.role[0]);
    }
  }, []);

  const [tenantId, settenantId] = useState(() => {
    return tenantIds.filter(
      (ele) =>
        ele.code == (searchParams?.tenantId != undefined ? { code: searchParams?.tenantId } : { code: Digit.ULBService.getCurrentTenantId() })?.code
    )[0];
  });

  const { isLoading, isError, errors, data: data, ...rest } = Digit.Hooks.hrms.useHrmsMDMS(
    tenantId ? tenantId.code : searchParams?.tenantId,
    "egov-hrms",
    "HRMSRolesandDesignation"
  );

  const [departments, setDepartments] = useState(() => {
    return { departments: null };
  });

  const [roles, setRoles] = useState(() => {
    return { roles: null };
  });

  const [isActive, setIsactive] = useState(() => {
    return { isActive: true };
  });



  useEffect(() => {
    if (tenantId.code) {
      setSearchParams({ ..._searchParams, tenantId: tenantId.code });
    }
  }, []);

  useEffect(() => {
    if (filters.role && filters.role.length > 0) {
      let res = [];
      filters.role.forEach((ele) => {
        res.push(ele.code);
      });

      setSearchParams({ ..._searchParams, roles: [...res].join(",") });
      if (filters.role && filters.role.length > 1) {
        let res = [];
        filters.role.forEach((ele) => {
          res.push(ele.code);
        });
        setSearchParams({ ..._searchParams, roles: [...res].join(",") });
      }
    }
    else if (filters.role && filters.role.length === 0) {
      setSearchParams({ ..._searchParams, roles: undefined });
    }
  }, []);

  useEffect(() => {
    if (departments) {
      setSearchParams({ ..._searchParams, departments: departments.code });
    }
  }, []);

  useEffect(() => {
    if (roles) {
      setSearchParams({ ..._searchParams, roles: roles.code });
    }
  }, []);

  const ifExists = (list, key) => {
    return list?.filter((object) => object.code === key.code).length;
  };

  useEffect(() => {
    if (isActive) {
      setSearchParams({ ..._searchParams, isActive: isActive.code });
    }
  }, []);
  const clearAll = () => {
    onFilterChange({ delete: Object.keys(searchParams) });
    settenantId(tenantIds.filter((ele) => ele.code == Digit.ULBService.getCurrentTenantId())[0]);
    setDepartments(null);
    setRoles(null);
    setIsactive(null);
    props?.onClose?.();
    onSelectFilterRoles({ role: [] });
  };

  const GetSelectOptions = (lable, options, selected, select, optionKey, onRemove, key) => {
    selected = selected || { [optionKey]: " ", code: "" };
    return (
      <div>
        <div className="filter-label">{lable}</div>
        {<Controller
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
        />}
        <div className="tag-container">
          {filters?.role?.length > 0 &&
            filters?.role?.map((value, index) => {
              return <RemoveableTag key={index} text={`${value[optionKey].slice(0, 22)} ...`} onClick={() => onRemove(index, key)} />;
            })}
        </div>
      </div>
    );
  };
  //-------------------------------------------------------
  const { fetchBMCCommon } = useBMCCommon();
  const { data: schemeBMCData } = fetchBMCCommon(); const handleApply = () => {
    console.log('check 01')

    const payload = {
      state: isActive?.code === true ? "APPLIED" : 
              isActive?.code === false && filters.role[0]?.name === "VERIFIED" ? "VERIFIED" :
              isActive?.code === false && filters.role[0]?.name === "APPROVED" ? "APPROVED" :
              isActive?.code === false && filters.role[0]?.name === "SELECTED" ? "SELECTED" :
              isActive?.code === false && filters.role[0]?.name === "REJECTED" ? "REJECTED" : 
              null,
      courseid: selectedDetail && detailOptions.find(d => d.value === selectedDetail)?.type === "course" 
                ? selectedDetail 
                : null,
      machineid: selectedDetail && detailOptions.find(d => d.value === selectedDetail)?.type === "machine" 
                 ? selectedDetail 
                 : null,
      schemeId: selectedScheme ? selectedScheme : 1, 
      ward: null,
      zone: null, 
      city: null, 
      createddate: null, 
      enddate: null
    };
  
    console.log('Filtering payload:', payload);
  
    schemeBMCData.mutate(payload, {
      onSuccess: (filteredData) => {
        console.log('Filtered API data:', filteredData);
        // You might want to update parent component state or trigger a re-render
        onFilterChange(filteredData);
      },
      onError: (error) => {
        console.error('Error filtering data:', error);
        // Handle error - maybe show a toast or error message
      },
    });
  };



  return (
    <React.Fragment>
      <div className="filter">
        <div className="filter-card">
          <div className="heading">
            <div className="filter-label" style={{ display: "flex", alignItems: "center" }}>
              <span>
                <svg width="17" height="17" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M0.66666 2.48016C3.35999 5.9335 8.33333 12.3335 8.33333 12.3335V20.3335C8.33333 21.0668 8.93333 21.6668 9.66666 21.6668H12.3333C13.0667 21.6668 13.6667 21.0668 13.6667 20.3335V12.3335C13.6667 12.3335 18.6267 5.9335 21.32 2.48016C22 1.60016 21.3733 0.333496 20.2667 0.333496H1.71999C0.613327 0.333496 -0.01334 1.60016 0.66666 2.48016Z"
                    fill="#505A5F"
                  />
                </svg>
              </span>
              <span>{t("BMC_COMMON_FILTER")}:</span>{" "}
            </div>
            <div className="clearAll" onClick={clearAll}>
              {t("HR_COMMON_CLEAR_ALL")}
            </div>
            {props.type === "desktop" && (
              <span className="clear-search" onClick={clearAll} style={{ border: "1px solid #e0e0e0", padding: "6px" }}>
                <svg width="17" height="17" viewBox="0 0 16 22" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M8 5V8L12 4L8 0V3C3.58 3 0 6.58 0 11C0 12.57 0.46 14.03 1.24 15.26L2.7 13.8C2.25 12.97 2 12.01 2 11C2 7.69 4.69 5 8 5ZM14.76 6.74L13.3 8.2C13.74 9.04 14 9.99 14 11C14 14.31 11.31 17 8 17V14L4 18L8 22V19C12.42 19 16 15.42 16 11C16 9.43 15.54 7.97 14.76 6.74Z"
                    fill="#505A5F"
                  />
                </svg>
              </span>
            )}
            {props.type === "mobile" && (
              <span onClick={props.onClose}>BMC_WELFARE_APPLICATIONS

                <CloseSvg />
              </span>
            )}
          </div>
          <div>
            {/* <div>
              <div className="filter-label">{t("HR_ULB_LABEL")}</div>
              <Dropdown
                option={[...getCityThatUserhasAccess(tenantIds)?.sort((x, y) => x?.name?.localeCompare(y?.name)).map(city => { return { ...city, i18text: Digit.Utils.locale.getCityLocale(city.code) } })]}
                selected={tenantId}
                select={settenantId}
                optionKey={"i18text"}
                t={t}
              />
            </div> */}
            <div>
              <div className="filter-label">{t("BMC_SCHEME")}</div>
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
            </div>
            <div>
              <div>
                {GetSelectOptions(
                  t("BMC_SUB_SCHEME"),
                  Digit.Utils.locale.convertToLocaleData(data?.MdmsRes["ACCESSCONTROL-ROLES"]?.roles, 'ACCESSCONTROL_ROLES_ROLES', t),
                  selectedRoles,
                  onSelectRoles,
                  "i18text",
                  onRemove,
                  "role"
                )}
              </div>
            </div>
            <div>
              <div className="filter-label">{t("BMC_APPLICATION_STATUS")}</div>
              <RadioButtons
                onSelect={setIsactive}
                selected={isActive}
                selectedOption={isActive}
                optionsKey="name"
                options={[
                  { code: true, name: t("BMC_APPLIED") },
                  { code: false, name: t("BMC_APPROVED") },
                  { code: false, name: t("BMC_VERIFIED") },
                  { code: false, name: t("BMC_SELECTED") },
                  { code: false, name: t("BMC_REJECTED") },



                ]}
              />
              {props.type !== "mobile" && <div>
                <SubmitBar onSubmit={handleApply} label={t("BMC_COMMON_APPLY")} />
              </div>}
            </div>
          </div>
        </div>
      </div>
      {props.type === "mobile" && (
        <ActionBar>
          <ApplyFilterBar
            submit={false}
            labelLink={t("ES_COMMON_CLEAR_ALL")}
            buttonLink={t("ES_COMMON_FILTER")}
            onClear={clearAll}
            onSubmit={() => {
              onFilterChange(_searchParams)
              props?.onClose?.()
            }}
            style={{ flex: 1 }}
          />
        </ActionBar>
      )}
    </React.Fragment>
  );
};

export default Filter;
