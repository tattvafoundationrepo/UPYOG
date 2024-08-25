import { EmployeeModuleCard, PersonIcon } from "@upyog/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";

const DEONARCard = () => {
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();

  const formatLabel = (label) => {
    const [beforeDash, afterDash] = label.split(" - ");
    return (
      <React.Fragment>
        <strong>{beforeDash}</strong>
        <br />
        {afterDash}
      </React.Fragment>
    );
  };

  const propsForModuleCard = {
    Icon: <PersonIcon />,
    moduleName: t("DEONAR"),
    kpis: [],
    links: [
      {
        label: formatLabel(t("SECURITY CHECK - ARRIVAL")),
        link: `/digit-ui/employee/deonar/securitycheck`,
      },
      {
        label: t("PARKING FEE"),
        link: `/digit-ui/employee/deonar/parking`,
      },
      {
        label: t("REMOVAL"),
        link: `/digit-ui/employee/deonar/removal`,
      },
      {
        label: formatLabel(t("COLLECTION POINT - ASSIGN SHOPKEEPER")),
        link: `/digit-ui/employee/deonar/assignshopkeeper`,
      },
      {
        label: t("ENTRY FEE"),
        link: `/digit-ui/employee/deonar/entryfee`,
      },
      {
        label: t("REMOVAL FEE"),
        link: `/digit-ui/employee/deonar/removalfee`,
      },
      {
        label: t("STABLING FEE"),
        link: `/digit-ui/employee/deonar/stablingfee`,
      },
      {
        label: formatLabel(t("INSPECTION POINT - ANTE MORTEM INSPECTION")),
        link: `/digit-ui/employee/deonar/antemorteminspection`,
      },
      {
        label: t("RE-ANTE MORTEM INSPECTION"),
        link: `/digit-ui/employee/deonar/reantemorteminspection`,
      },
      {
        label: t("BEFORE SLAUGHTER ANTE MORTEM INSPECTION"),
        link: `/digit-ui/employee/deonar/antemortembeforeslaughterinspection`,
      },
      {
        label: t("POST MORTEM INSPECTION"),
        link: `/digit-ui/employee/deonar/postmorteminspection`,
      },
      {
        label: formatLabel(t("SLAUGHTER RECOVERY POINT - SLAUGHTER FEE RECOVERY")),
        link: `/digit-ui/employee/deonar/slaughterfeerecovery`,
      },
      {
        label: formatLabel(t("DELIVERY POINT - VEHICLE WASHING CHARGE COLLECTION")),
        link: `/digit-ui/employee/deonar/vehiclewashing`,
      },
      {
        label: t("WEIGHING CHARGE"),
        link: `/digit-ui/employee/deonar/weighingcharge`,
      },
      {
        label: t("PENALTY CHARGE"),
        link: `/digit-ui/employee/deonar/penaltyCharge`,
      },
      {
        label: t("GATE PASS"),
        link: `/digit-ui/employee/deonar/gatePass`,
      },
    ],
    longModuleName: false,
  };
  return <EmployeeModuleCard {...propsForModuleCard} />;
};

export default DEONARCard;
