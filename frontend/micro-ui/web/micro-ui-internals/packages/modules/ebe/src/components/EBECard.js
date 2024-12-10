import { EmployeeModuleCard, PersonIcon } from "@upyog/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";

const EBECard = () => {
  const { t } = useTranslation();
  const ADMIN = Digit.Utils.EBEAccess();
  if (!ADMIN) {
    return null;
  }
  const tenantId = Digit.ULBService.getCurrentTenantId();
  // const { isLoading, isError, error, data, ...restapply } = Digit.Hooks.bmc.useAppCount({action:null});
  
  let KPIForBMC = [
    // {
    //   count: isLoading ? "-" : data?.count?.APPROVE || 0,
    //   label: t("TOTAL_APPROVE"),
    //   link: `/digit-ui/employee/bmc/inbox`,
    //   role: "SUPERUSER"
    // }
  ];

  let propsForBMC = [
    {
      label: t("Pre_Enquiry"),
      link: `/digit-ui/employee/ebe/PEList`,
      role: "SUPERUSER"
    },
    {
      label: t("DE_Enquiry"),
      link: `/digit-ui/employee/ebe/DEList`,
      role: "SUPERUSER"
    }
  ]

  propsForBMC = propsForBMC.filter(link => link.role && Digit.Utils.didEmployeeHasRole(link.role));
  KPIForBMC = KPIForBMC.filter(link => link.role && Digit.Utils.didEmployeeHasRole(link.role));

  const propsForModuleCard = {
    Icon: <PersonIcon />,
    moduleName: t("EBE"),
    kpis: [...KPIForBMC],
    links: [
      ...propsForBMC,
      {
        label: t("Inbox"),
        link: `/digit-ui/employee/bmc/inbox`
      }
    ],
    longModuleName: true
  };
  return <EmployeeModuleCard {...propsForModuleCard} />;
};

export default EBECard;