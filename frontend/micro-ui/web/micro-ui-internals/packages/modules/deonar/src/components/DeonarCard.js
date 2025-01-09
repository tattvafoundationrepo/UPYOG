import { EmployeeModuleCard, PersonIcon } from "@upyog/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";

const DEONARCard = () => {
  const { t } = useTranslation();
  const ADMIN = Digit.Utils.DEONARAccess();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  if (!ADMIN) {
    return null;
  }
  //const { isLoading, isError, error, data, ...restapply } = Digit.Hooks.bmc.useAppCount({action:null});

  let KPIForDEONAR = [];

  let propsForDEONAR = [
    {
      label: t("SECURITY CHECK - ARRIVAL"),
      link: `/digit-ui/employee/deonar/securitycheck`,
      roles: ["SUPERUSER","DEONAR_COUNTER"]
    },
    {
      label: t("STABLING"),
      link: `/digit-ui/employee/deonar/stabling`,
      roles: ["SUPERUSER","DEONAR_GENERIC_GAWAL"]
    },
    {
      label: t("SHOPKEEPER"),
      link: `/digit-ui/employee/deonar/s`,
      roles: ["SUPERUSER","DEONAR_GENERIC_SHOPKEEPER"]
    },
    {
      label: t("Helkari"),
      link: `/digit-ui/employee/deonar/Helkari`,
      roles: ["SUPERUSER","DEONAR_GENERIC_HELKARI"]
    },
    {
      label: t("TRADING"),
      link: `/digit-ui/employee/deonar/trading`,
      roles: ["SUPERUSER","DEONAR_GENERIC_DAWANWALA"]
    },
    {
      label: t("INSPECTION"),
      link: `/digit-ui/employee/deonar/Inspection`,
      roles: ["SUPERUSER","DEONAR_INSPECTION"]
    },
    {
      label: t("BOOKING_SLAUGHTER"),
      link: `/digit-ui/employee/deonar/slaughtering`,
      roles: ["SUPERUSER","DEONAR_SLAUGHTER"]
    },
    {
      label: t("SLAUGHTERING (EMR)"),
      link: `/digit-ui/employee/deonar/slaughteringEmr`,
      roles: ["SUPERUSER","DEONAR_SLAUGHTER","DEONAR_SLAUGHTER_EMR"]
    },
    {
      label: t("SLAUGHTERING (NOR)"),
      link: `/digit-ui/employee/deonar/slaughteringNor`,
      roles: ["SUPERUSER","DEONAR_SLAUGHTER","DEONAR_SLAUGHTER_NOR"]
    },
    {
      label: t("SLAUGHTERING (EXT)"),
      link: `/digit-ui/employee/deonar/slaughteringExt`,
      roles: ["SUPERUSER","DEONAR_SLAUGHTER","DEONAR_SLAUGHTER_EXT"]
    },
    {
      label: t("COLLECTION"),
      link: `/digit-ui/employee/deonar/feeCollection`,
      roles: ["SUPERUSER","DEONAR_COLLECTION",]
    },
    {
      label: t("PARKING"),
      link: `/digit-ui/employee/deonar/parking`,
      roles: ["SUPERUSER","DEONAR_GENERIC"]
    },
    // {
    //   label: t("REMOVAL"),
    //   link: `/digit-ui/employee/deonar/removal`,
    //   roles: ["SUPERUSER","DEONAR_GENERIC"]
    // },
    // {
    //   label: t("REMOVAL FEE"),
    //   link: `/digit-ui/employee/deonar/removalfee`,
    //   roles: ["SUPERUSER","DEONAR_COLLECTION"]
    // },
    // {
    //   label: t("SLAUGHTER FEE RECOVERY"),
    //   link: `/digit-ui/employee/deonar/slaughterfeerecovery`,
    //   roles: ["SUPERUSER","DEONAR_COLLECTION"]
    // },
    // {
    //   label: t("VEHICLE WASHING CHARGE COLLECTION"),
    //   link: `/digit-ui/employee/deonar/vehiclewashing`,
    //   roles: ["SUPERUSER","DEONAR_GENERIC"]
    // },
    // {
    //   label: t("WEIGHING CHARGE"),
    //   link: `/digit-ui/employee/deonar/weighingcharge`,
    //   roles: ["SUPERUSER","DEONAR_GENERIC"]
    // },
    {
      label: t("PENALTY CHARGE"),
      link: `/digit-ui/employee/deonar/penaltyCharge`,
      roles: ["SUPERUSER","DEONAR_COLLECTION_PENALTY"]
    },
    // {
    //   label: t("GATE PASS"),
    //   link: `/digit-ui/employee/deonar/gatePass`,
    //   roles: ["SUPERUSER","DEONAR_COUNTER_GATEPASS"]
    // },
    {
      label: t("GATE PASS"),
      link: `/digit-ui/employee/deonar/gate`,
      roles: ["SUPERUSER","DEONAR_COUNTER_GATEPASS"]
    },
    {
      label: t("Stake Holder"),
      link: `/digit-ui/employee/deonar/stakeholder`,
      roles: ["SUPERUSER"]
    },
    {
      label: t("INBOX"),
      link: `/digit-ui/employee/deonar/Inbox`,
      roles: ["SUPERUSER"]
    },
    {
      label: t("REMOVAL REPORT"),
      link: `/digit-ui/employee/deonar/removalReport`,
      roles: ["SUPERUSER"]
    },
    {
      label: t("WASHING"),
      link: `/digit-ui/employee/deonar/washing`,
      roles: ["SUPERUSER","DEONAR_GENERIC"]
    },
    {
      label: t("Citizen Assignment"),
      link: `/digit-ui/employee/deonar/citizenAssignment`,
      roles: ["SUPERUSER","DEONAR_GENERIC_CITIZEN"]
    },
    {
      label: t("Citizen Gate Pass"),
      link: `/digit-ui/employee/deonar/citizenGatePass`,
      roles: ["SUPERUSER","DEONAR_COUNTER"]
    }
  ];

  //propsForDEONAR = propsForDEONAR.filter(link => link.role && Digit.Utils.didEmployeeHasRole(link.role));
  propsForDEONAR = propsForDEONAR.filter(link => 
    link.roles && link.roles.some(role => Digit.Utils.didEmployeeHasRole(role))
  );
  //KPIForDEONAR = KPIForDEONAR.filter(link => link.role && Digit.Utils.didEmployeeHasRole(link.role));

  const propsForModuleCard = {
    Icon: <PersonIcon />,
    moduleName: t("DEONAR"),
    kpis: [...KPIForDEONAR],
    links: [
      ...propsForDEONAR,
    ],
    longModuleName: true
  };
  return <EmployeeModuleCard {...propsForModuleCard} />;
};

export default DEONARCard;