import React, { useState } from "react";
import { ArrowForward } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const FAQComponent = (props) => {
  const { question, answer, index, lastIndex, subAnswer, acrynom } = props;
  const [isOpen, toggleOpen] = useState(false);
  const { t } = useTranslation();

  return (
    <div className="faqs border-none" onClick={() => toggleOpen(!isOpen)}>
      <div
        className="faq-question"
        style={{
          justifyContent: t(question).length > 30 && isOpen ? "revert" : "space-between",
          display: Digit.Utils.browser.isMobile() && t(question).length > 30 && isOpen ? "block" : "flex",
        }}
      >
        <span style={{ fontWeight: 700 }}>{`${index}. ` + t(question)}</span>
        <span className={isOpen ? "faqicon rotate" : "faqicon"} style={{ float: "right" }}>
          {isOpen ? <ArrowForward /> : <ArrowForward />}
        </span>
      </div>

      <div className="faq-answer" style={isOpen ? { display: "block" } : { display: "none" }}>
        <div style={{ marginTop: "-20px" }}>
          {answer?.map((obj, i) =>
            obj.ans ? (
              <span key={i} style={{ color: "#000", marginTop: "20px", marginBottom: "20px" }}>
                {t(obj.ans)}
              </span>
            ) : null
          )}

          {answer?.map((obj, i) =>
            obj.point ? (
              <span key={i} style={{ color: "#000", marginLeft: "30px" }}>
                •<div style={{ marginTop: "-21px", marginLeft: "15px" }}>{t(obj.point)}</div>
              </span>
            ) : null
          )}
        </div>
      </div>

      {!lastIndex && <div className="cs-box-border" />}
    </div>
  );
};

export default FAQComponent;
