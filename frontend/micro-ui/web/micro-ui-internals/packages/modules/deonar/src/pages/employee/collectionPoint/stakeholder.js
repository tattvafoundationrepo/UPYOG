import React, { useEffect, useState } from 'react';
import { useTranslation } from "react-i18next";
import { Controller, useForm } from "react-hook-form";

import MainFormHeader from "../commonFormFields/formMainHeading";
import { CardLabel, Dropdown, LabelFieldPair } from '@upyog/digit-ui-react-components';
import MultiColumnDropdown from '../commonFormFields/multiColumnDropdown';

import useDeonarCommon from '@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar';
import LicenseNumberField from '../commonFormFields/licenseNumber';
import ReceiverField from '../commonFormFields/receiverName';

const Stakeholder = () => {
  const { t } = useTranslation();
  const [data, setData] = useState({});

  const [options, setOptions] = useState([]);
  const [animal, setAnimal] = useState([]);
  const [selectedOption, setSelectedOption] = useState([]);


  const {
    control,
    formState: { errors, isValid, dirtyFields },
  } = useForm();



  const { fetchDeonarCommon } = useDeonarCommon();
  const useFetchOptions = (optionType) => {
    const { data } = fetchDeonarCommon({
      CommonSearchCriteria: {
        Option: optionType,
      },
    });
    return data
      ? data.CommonDetails.map((item) => ({
        name: item.name,
        value: item.id,
      }))
      : [];
  };

  const stakeholderData = useFetchOptions("stakeholder");
  const stakeholderAnimal = useFetchOptions("animal");


  useEffect(() => {
    setOptions(stakeholderData.map((item) => item.name || []));
    setAnimal(stakeholderAnimal.map((item) => item.name || []));
  }, [stakeholderData, stakeholderAnimal]);

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form >
          <MainFormHeader title={t("DEONAR_STAKEHOLDER_DETAILS")} />
          <div className="bmc-card-row" style={{ display: "flex", gap: "10px" }}>
            <div className="bmc-row-card-header" style={{ display: "flex", flexDirection: "column" }}>
              <div className="bmc-card-row">
                <div className="bmc-col3-card">
                  <LabelFieldPair>
                    <CardLabel className="bmc-label">{t("DEONAR_STACKHOLDER_NAME")}</CardLabel>
                    <Controller
                      control={control}
                      name="stackholderName"
                      rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                      render={(props) => (
                        <div>
                          <Dropdown
                            name="stackholderName"
                            selected={props.value}
                            select={(value) => {
                              props.onChange(value);
                              const newData = {
                                ...data,
                                stackholderName: value,
                              };
                              setData(newData);
                            }}
                            onBlur={props.onBlur}
                            option={stakeholderData}
                            optionKey="name"
                            t={t}
                            placeholder={t("DEONAR_STACKHOLDER_NAME")}
                          />
                        </div>
                      )}
                    />
                  </LabelFieldPair>
                </div>
                <div className="bmc-col3-card">
                  <LabelFieldPair>
                    <CardLabel className="bmc-label">{t("DEONAR_ANIMAL_NAME")}</CardLabel>
                    <Controller
                      control={control}
                      name="animalName"
                      rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                      render={(props) => (
                        <div>
                          <Dropdown
                            name="animalName"
                            selected={props.value}
                            select={(value) => {
                              props.onChange(value);
                              const newData = {
                                ...data,
                                animalName: value,
                              };
                              setData(newData);
                            }}
                            onBlur={props.onBlur}
                            option={stakeholderAnimal} // Correctly pass the mapped `animal` options
                            optionKey="name"
                            t={t}
                            placeholder={t("DEONAR_ANIMAL_NAME")}
                          />

                        </div>
                      )}
                    />
                  </LabelFieldPair>
                </div>
                <ReceiverField control={control} setData={setData} data={data} label={t("Name")} name={"name"} />

                <LicenseNumberField control={control} data={data} setData={setData} disabled={false} style={null} />

              </div>
            </div>
          </div>
        </form>
      </div>
    </React.Fragment>
  )
}
export default Stakeholder;