import React, { useState } from "react";
import { ArrowForward } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const HowItWorkComponent = ({ question, answer, subAnswer, index, lastIndex }) => {
  const [isOpen, toggleOpen] = useState(false);
  const { t } = useTranslation(); // To handle translation

  return (
    <div className="faqs border-none" onClick={() => toggleOpen(!isOpen)}>
      <div
        className="faq-question"
        style={{
          justifyContent: t(question).length > 30 && isOpen ? "revert" : "space-between",
          display: t(question).length > 30 && isOpen ? "block" : "flex",
        }}
      >
        <span style={{ fontWeight: 700 }}>{`${index}. ` + t(question)}</span>
        <span className={isOpen ? "faqicon rotate" : "faqicon"} style={{ float: "right" }}>
          {isOpen ? <ArrowForward /> : <ArrowForward />}
        </span>
      </div>

      <div className="faq-answer" style={isOpen ? { display: "block" } : { display: "none" }}>
        <div style={{ marginTop: "-20px" }}>
          {/* Mapping the answer */}
          {answer?.map((obj, i) => (
            <span key={i} style={{ color: "#000", marginTop: "20px", marginBottom: "20px" }}>
              {t(obj.ans)}
            </span>
          ))}

          {/* Mapping the acronyms
          {acrynoms?.map((obj, i) => (
            <div key={i}>
              <span
                style={{
                  color: "#000",
                  marginTop: index === 1 ? (i === 0 ? "20px" : "0px") : "20px",
                  marginBottom: index === 1 ? (i === 0 ? "20px" : "0px") : "20px",
                }}
              >
                {t(obj.acr)}
              </span>
              <span
                style={{
                  color: "#000",
                  marginTop: index === 1 ? (i === 0 ? "-40px" : "-20px") : "20px",
                  marginBottom: index === 1 ? (i === 14 ? "20px" : "0px") : "20px",
                }}
              >
                {t(obj.fullForm)}
              </span>
            </div>
          ))} */}

          {/* Mapping the bullet points */}
          {answer?.map((obj, i) => (
            <span key={i} style={{ color: "#000", marginLeft: "30px" }}>
              {obj.point ? "•" : null}
              <div style={{ marginTop: "-21px", marginLeft: "15px" }}>{t(obj.point)}</div>
            </span>
          ))}
        </div>

        <div>
          {/* Mapping the sub-answers */}
          {subAnswer?.map((obj, i) => (
            <span key={i} style={{ color: "#000", marginBottom: "20px" }}>
              {t(obj.ans)}
            </span>
          ))}

          {/* Mapping sub-answer bullet points */}
          {subAnswer?.map((obj, i) => (
            <span key={i} style={{ color: "#000", marginLeft: "30px" }}>
              {obj.point ? "•" : null}
              <div style={{ marginTop: "-21px", marginLeft: "15px" }}>{t(obj.point)}</div>
            </span>
          ))}
        </div>
      </div>
      {!lastIndex ? <div className="cs-box-border" /> : null}
    </div>
  );
};

export default HowItWorkComponent;
