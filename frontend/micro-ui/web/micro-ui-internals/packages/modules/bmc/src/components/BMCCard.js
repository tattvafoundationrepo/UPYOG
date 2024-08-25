import { EmployeeModuleCard, PersonIcon } from "@upyog/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";

const BMCCard = () => {
 
    const { t } = useTranslation();
    const tenantId = Digit.ULBService.getCurrentTenantId();
    
    const propsForModuleCard = {
      Icon: <PersonIcon/>,
      moduleName: t("BMC"),
      kpis: [],
      links: [
        {
          label: t("VERIFY APPLICATIONS"),
          link: `/digit-ui/employee/bmc/aadhaarverify`,
  
        }
        ,
        {
          label: t("RANDOMIZE APPLICATIONS"),
          link: `/digit-ui/employee/bmc/randmization`,
  
        },
        {
          label: t("APPROVE APPLICATIONS"),
          link: `/digit-ui/employee/bmc/crossverify`,
  
        },
        {
          label: t("APPROVE CROSSED VERIFIED APPLICATIONS"),
          link: `/digit-ui/employee/bmc/approve`,
  
        },
    ],longModuleName:false};
    return <EmployeeModuleCard {...propsForModuleCard} />;
  };
  
  export default BMCCard;