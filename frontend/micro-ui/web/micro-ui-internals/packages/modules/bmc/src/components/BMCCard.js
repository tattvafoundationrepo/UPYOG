import { EmployeeModuleCard, PersonIcon } from "@upyog/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";

const BMCCard = () => {
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const isAppVerifier = Digit.Utils.didEmployeeHasRole("APP_VERIFIER");
  const { isLoading, isError, error, data, ...restapply } = Digit.Hooks.bmc.useAppCount({ action: null, forVerify: isAppVerifier ? true : false });
  const ADMIN = Digit.Utils.BMCAccess();
  if (!ADMIN) {
    return null;
  }
  let KPIForBMC = [
    {
      count: isLoading ? "-" : data?.count?.APPLY || 0,
      label: t("TOTAL_APPLY"),
      link: `/digit-ui/employee`,
      role: "APP_VERIFIER",
    },
    {
      count: isLoading ? "-" : data?.count?.VERIFY || 0 + data?.count?.RANDOMIZE || 0,
      label: t("TOTAL_VERIFY"),
      link: `/digit-ui/employee`,
      role: "APP_VERIFIER",
    },
    {
      count: isLoading ? "-" : data?.count?.RANDOMIZE || 0,
      label: t("TOTAL_RANDOMIZE"),
      link: `/digit-ui/employee`,
      role: "APP_RANDOMIZER",
    },
    {
      count: isLoading ? "-" : data?.count?.SELECTED || 0,
      label: t("TOTAL_SELECTED"),
      link: `/digit-ui/employee`,
      role: "APP_RANDOMIZER",
    },
    {
      count: isLoading ? "-" : (data?.count?.VERIFY || 0) + data?.count?.SELECTED || 0,
      label: t("TOTAL_VERIFY"),
      link: `/digit-ui/employee`,
      role: "APP_APPROVER",
    },
    {
      count: isLoading ? "-" : data?.count?.APPROVE || 0,
      label: t("TOTAL_APPROVE"),
      link: `/digit-ui/employee`,
      role: "APP_APPROVER",
    },
    {
      count: isLoading ? "-" : data?.count?.APPLY || 0,
      label: t("TOTAL_APPLY"),
      link: `/digit-ui/employee`,
      role: "SUPERUSER",
    },
    {
      count: isLoading ? "-" : data?.count?.VERIFY || 0 + data?.count?.RANDOMIZE || 0,
      label: t("TOTAL_VERIFY"),
      link: `/digit-ui/employee`,
      role: "SUPERUSER",
    },
    {
      count: isLoading ? "-" : data?.count?.APPROVE || 0,
      label: t("TOTAL_APPROVE"),
      link: `/digit-ui/employee`,
      role: "SUPERUSER",
    },
  ];

  let propsForBMC = [
    {
      label: t("ACTION_TEST_VERIFY_APPLICATIONS"),
      link: `/digit-ui/employee/bmc/aadhaarverify`,
      role: "SUPERUSER",
    },
    {
      label: t("ACTION_TEST_VERIFY_APPLICATIONS"),
      link: `/digit-ui/employee/bmc/aadhaarverify`,
      role: "PLANNING_ADMIN",
    },
    {
      label: t("ACTION_TEST_VERIFY_APPLICATIONS"),
      link: `/digit-ui/employee/bmc/aadhaarverify`,
      role: "APP_VERIFIER",
    },
    {
      label: t("ACTION_TEST_RANDOMIZE_APPLICATIONS"),
      link: `/digit-ui/employee/bmc/randmization`,
      role: "SUPERUSER",
    },
    {
      label: t("ACTION_TEST_RANDOMIZE_APPLICATIONS"),
      link: `/digit-ui/employee/bmc/randmization`,
      role: "PLANNING_ADMIN",
    },
    {
      label: t("ACTION_TEST_RANDOMIZE_APPLICATIONS"),
      link: `/digit-ui/employee/bmc/randmization`,
      role: "APP_RANDOMIZER",
    },
    {
      label: t("ACTION_TEST_APPROVE_APPLICATIONS"),
      link: `/digit-ui/employee/bmc/approve`,
      role: "SUPERUSER",
    },
    {
      label: t("ACTION_TEST_APPROVE_APPLICATIONS"),
      link: `/digit-ui/employee/bmc/approve`,
      role: "PLANNING_ADMIN",
    },
    {
      label: t("ACTION_TEST_APPROVE_APPLICATIONS"),
      link: `/digit-ui/employee/bmc/approve`,
      role: "APP_APPROVER",
    },
    {
      label: t("INBOX"),
      link: `/digit-ui/employee/bmc/inbox`,
      role: "SUPERUSER",
    },
    {
      label: t("INBOX"),
      link: `/digit-ui/employee/bmc/inbox`,
      role: "PLANNING_ADMIN",
    },
  ];

  propsForBMC = propsForBMC.filter((link) => link.role && Digit.Utils.didEmployeeHasRole(link.role));
  KPIForBMC = KPIForBMC.filter((link) => link.role && Digit.Utils.didEmployeeHasRole(link.role));

  const propsForModuleCard = {
    Icon: <PersonIcon />,
    moduleName: t("BMC"),
    kpis: [...KPIForBMC],
    links: [
      ...propsForBMC
    ],
    longModuleName: true,
  };
  return <EmployeeModuleCard {...propsForModuleCard} />;
};

export default BMCCard;
