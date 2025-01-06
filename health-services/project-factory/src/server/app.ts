import express from "express";
import * as bodyParser from "body-parser";
import config from "./config";
import { requestMiddleware } from "./utils/middlewares";
import {
  errorLogger,
  errorResponder,
  invalidPathHandler,
  throwError,
} from "./utils/genericUtils";
import { tracingMiddleware } from "./tracing";
import { createProxyMiddleware } from "http-proxy-middleware";
import * as v8 from "v8";
import { logger } from "./utils/logger";
import { fetchLocalesFromMDMS, fetchProjectTypesFromMDMS, handleTemplateCreation } from "./utils/templateUtils";

const printMemoryInMB = (memoryInBytes: number) => {
  const memoryInMB = memoryInBytes / (1024 * 1024); // Convert bytes to MB
  return `${memoryInMB.toFixed(2)} MB`;
};

class App {
  public app: express.Application;
  public port: number;

  constructor(controllers: any, port: any) {
    this.app = express();
    this.port = port;

    this.initializeMiddlewares();
    this.initializeControllers(controllers);
    this.app.use(invalidPathHandler);

    // Global error handling for uncaught exceptions
    process.on("uncaughtException", (err) => {
      console.error("Unhandled Exception:", err);
    });

    // Global error handling for unhandled promise rejections
    process.on("unhandledRejection", (reason, promise) => {
      console.error("Unhandled Rejection at:", promise, "reason:", reason);
    });
  }

  private initializeMiddlewares() {
    this.app.use(
      bodyParser.json({ limit: config.app.incomingRequestPayloadLimit })
    );
    this.app.use(
      bodyParser.urlencoded({
        limit: config.app.incomingRequestPayloadLimit,
        extended: true,
      })
    );
    // this.app.use(bodyParser.json());
    this.app.use(tracingMiddleware);
    this.app.use(requestMiddleware);
    this.app.use(errorLogger);
    this.app.use(errorResponder);
    this.app.use(
      "/tracing",
      createProxyMiddleware({
        target: "http://localhost:16686",
        changeOrigin: true,
        pathRewrite: {
          "^/tracing": "/",
        },
      })
    );
  }

  private initializeControllers(controllers: any) {
    controllers.forEach((controller: any) => {
      this.app.use(config.app?.contextPath, controller.router);
    });
  }

  public listen() {
    this.app.listen(this.port, () => {
      logger.info(`App listening on the port ${this.port}`);
      // Add periodic monitoring
      setInterval(() => {
        const stats = v8.getHeapStatistics();
        const usedHeapSize = stats.used_heap_size;
        const heapLimit = stats.heap_size_limit;

        logger.debug(
          JSON.stringify({
            "Heap Usage": {
              used: printMemoryInMB(usedHeapSize),
              limit: printMemoryInMB(heapLimit),
              percentage: ((usedHeapSize / heapLimit) * 100).toFixed(2),
            },
          })
        );

        // Alert if memory usage is above 80%
        if (usedHeapSize / heapLimit > 0.8) {
          logger.warn("High memory usage detected");
        }
      }, 5 * 60 * 1000); // Every 5 minutes
      logger.info(
        "Current App's Heap Configuration Total Available :",
        printMemoryInMB(v8.getHeapStatistics()?.total_available_size),
        " max limit set to : ",
        printMemoryInMB(v8.getHeapStatistics()?.heap_size_limit)
      );

      this.postStartupLogic();  // This is called after the server starts and after all monitoring tasks
    });
  }

  async postStartupLogic() {
    logger.info("Server started and post-startup tasks executed.");
  
    // Fetch project types from MDMS
    const projectTypes = await fetchProjectTypesFromMDMS();
    if (!projectTypes?.length) {
      throwError(
        "MDMS",
        500,
        "MDMS_DATA_NOT_FOUND_ERROR",
        `MDMS data not configured for ${config.moduleNameForProjectTypes}.${config.masterNameForProjectTypes}`
      );
    }
  
    // Fetch locales from MDMS
    const allLocales = await fetchLocalesFromMDMS();
    if (!allLocales?.length) {
      throwError(
        "MDMS",
        500,
        "MDMS_DATA_NOT_FOUND_ERROR",
        `MDMS locales not configured for ${config.commonMastersModule}.${config.stateInfoMasters}`
      );
    }
  
    const allTypesOfTemplate = config?.allTypesOfTemplate;
  
    // Check and create templates if not exist
    for (const locale of allLocales) {
      for (const projectType of projectTypes) {
        for (const type of allTypesOfTemplate) {
          await handleTemplateCreation(locale, projectType, type);
        }
      }
    }
  }

}

export default App;
